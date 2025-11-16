package com.bookbuddy.service;

import com.bookbuddy.dto.BookDTO;
import com.bookbuddy.dto.external.GoogleBooksResponse;
import com.bookbuddy.dto.external.GoogleBooksResponse.GoogleBookItem;
import com.bookbuddy.dto.external.GoogleBooksResponse.VolumeInfo;
import lombok.extern.slf4j.Slf4j;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoogleBooksService {

    private final WebClient.Builder webClientBuilder;
    private final String baseUrl;
    private final String apiKey;
    // Caffeine cache to avoid hitting Google Books too often
    private final Cache<String, List<BookDTO>> cache;
    // Fallback cover image when Google Books returns none
    private static final String FALLBACK_COVER_URL = "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiUsbbnb2SYp053Rxou8To35w3VmcQ6vdDiPOoJLEWj6K1xN4eEUP-DtU-eFBpCbds5rHF7j5ps5HgQwfb8AR9jVQscuAx075_Z9mgALEeA1ifPxFza-GoSqA5_nRSznye0xNM7kCfZF010_ARn1gb6DMhXeByOC9u1_ljqwNgmvPNmRmZSpeNi7zSpXh25/s320/round-icons-sYodBkUvFBI-unsplash.jpg";
    // Temporary id generator for external results (negative values to avoid DB collisions)
    private final java.util.concurrent.atomic.AtomicLong tempIdCounter = new java.util.concurrent.atomic.AtomicLong(-1L);
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;

    private static final Counter NO_OP_COUNTER = new Counter() {
        @Override
        public void increment(double amount) {
            // no-op
        }

        @Override
        public double count() {
            return 0;
        }

        @Override
        public io.micrometer.core.instrument.Meter.Id getId() {
            return null;
        }

        @Override
        public void close() {
            // no-op
        }
    };

    public GoogleBooksService(
            WebClient.Builder webClientBuilder,
            @Value("${external.api.google-books.base-url}") String baseUrl,
            @Value("${external.api.google-books.api-key}") String apiKey,
            @Autowired(required = false) MeterRegistry meterRegistry) {
        this.webClientBuilder = webClientBuilder;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(1000)
                .build();
        if (meterRegistry != null) {
            this.cacheHitCounter = meterRegistry.counter("googlebooks.cache.hit");
            this.cacheMissCounter = meterRegistry.counter("googlebooks.cache.miss");
        } else {
            // Use no-op counters if registry is unavailable (e.g., in tests)
            this.cacheHitCounter = NO_OP_COUNTER;
            this.cacheMissCounter = NO_OP_COUNTER;
        }
    }

    public List<BookDTO> searchBooks(String query, int maxResults) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

            int cappedMax = Math.max(1, Math.min(maxResults, 40));

            String cacheKey = query + "|" + cappedMax;
            List<BookDTO> cached = cache.getIfPresent(cacheKey);
            if (cached != null) {
                cacheHitCounter.increment();
                return cached.stream()
                        .map(this::cloneDTOWithNewTempIdIfNeeded)
                        .collect(Collectors.toList());
            }
            cacheMissCounter.increment();

            GoogleBooksResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/volumes")
                            .queryParam("q", query)
                            .queryParam("maxResults", cappedMax)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.warn("Google Books API returned status {} for query={}", clientResponse.statusCode(), query);
                        return Mono.empty();
                    })
                    .bodyToMono(GoogleBooksResponse.class)
                    .onErrorResume(e -> {
                        log.error("Error calling Google Books API: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

                if (response == null || response.getItems() == null) {
                return new ArrayList<>();
                }

                List<BookDTO> results = response.getItems().stream()
                    .map(this::convertToBookDTO)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

                // cache a copy (without consuming temp ids for cached copy)
                cache.put(cacheKey, new ArrayList<>(results));

                return results.stream()
                    .map(this::cloneDTOWithNewTempIdIfNeeded)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error searching books: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public BookDTO getBookById(String googleBooksId) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

            GoogleBookItem item = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/volumes/{id}")
                            .queryParam("key", apiKey)
                            .build(googleBooksId))
                    .retrieve()
                    .bodyToMono(GoogleBookItem.class)
                    .onErrorResume(e -> {
                        log.error("Error fetching book from Google Books API: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            return item != null ? convertToBookDTO(item) : null;

        } catch (Exception e) {
            log.error("Error getting book by ID: {}", e.getMessage());
            return null;
        }
    }

    private BookDTO convertToBookDTO(GoogleBookItem item) {
        VolumeInfo info = item.getVolumeInfo();

        if (info == null) {
            log.warn("VolumeInfo is null for item: {}", item.getId());
            return null;
        }

        String title = (info.getTitle() != null && !info.getTitle().isBlank()) ? info.getTitle() : "Untitled";

        String isbn = null;
        if (info.getIndustryIdentifiers() != null) {
            isbn = info.getIndustryIdentifiers().stream()
                    .filter(id -> "ISBN_13".equals(id.getType()) || "ISBN_10".equals(id.getType()))
                    .map(GoogleBooksResponse.IndustryIdentifier::getIdentifier)
                    .findFirst()
                    .orElse(null);
        }

        String author = (info.getAuthors() != null && !info.getAuthors().isEmpty())
                ? String.join(", ", info.getAuthors())
                : "Unknown Author";

        String coverImage = null;
        if (info.getImageLinks() != null) {
            coverImage = info.getImageLinks().getThumbnail() != null
                    ? info.getImageLinks().getThumbnail()
                    : info.getImageLinks().getSmallThumbnail();
        }
        if (coverImage == null || coverImage.isBlank()) {
            coverImage = FALLBACK_COVER_URL;
        }

        return BookDTO.builder()
            // Assign a temporary unique id for external results so client can refer to them
            .id(tempIdCounter.getAndDecrement())
            .title(title)
                .author(author)
                .isbn(isbn)
                .description(info.getDescription())
                .publisher(info.getPublisher())
                .publishedDate(info.getPublishedDate())
                .pageCount(info.getPageCount())
                .coverImageUrl(coverImage)
                .categories(info.getCategories())
                .language(info.getLanguage())
                .googleBooksId(item.getId())
                .averageRating(info.getAverageRating() != null ? info.getAverageRating() : 0.0)
                .ratingsCount(info.getRatingsCount() != null ? info.getRatingsCount() : 0)
                .build();
    }

    private BookDTO cloneDTOWithNewTempIdIfNeeded(BookDTO dto) {
        if (dto == null) return null;
        // create a shallow clone but assign a fresh temporary id for each consumer
        return BookDTO.builder()
                .id(tempIdCounter.getAndDecrement())
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .isbn(dto.getIsbn())
                .description(dto.getDescription())
                .publisher(dto.getPublisher())
                .publishedDate(dto.getPublishedDate())
                .pageCount(dto.getPageCount())
                .coverImageUrl(dto.getCoverImageUrl())
                .categories(dto.getCategories())
                .language(dto.getLanguage())
                .googleBooksId(dto.getGoogleBooksId())
                .averageRating(dto.getAverageRating())
                .ratingsCount(dto.getRatingsCount())
                .build();
    }

}