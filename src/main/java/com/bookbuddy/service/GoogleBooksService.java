package com.bookbuddy.service;

import com.bookbuddy.dto.BookDTO;
import com.bookbuddy.dto.external.GoogleBooksResponse;
import com.bookbuddy.dto.external.GoogleBooksResponse.GoogleBookItem;
import com.bookbuddy.dto.external.GoogleBooksResponse.VolumeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleBooksService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${external.api.google-books.base-url}")
    private String baseUrl;
    
    @Value("${external.api.google-books.api-key}")
    private String apiKey;
    
    public List<BookDTO> searchBooks(String query, int maxResults) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
            
            GoogleBooksResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/volumes")
                            .queryParam("q", query)
                            .queryParam("maxResults", maxResults)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(GoogleBooksResponse.class)
                    .onErrorResume(e -> {
                        log.error("Error calling Google Books API: {}", e.getMessage());
                        return Mono.just(new GoogleBooksResponse());
                    })
                    .block();
            
            if (response == null || response.getItems() == null) {
                return new ArrayList<>();
            }
            
            return response.getItems().stream()
                    .map(this::convertToBookDTO)
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
        
        String title = info.getTitle() != null && !info.getTitle().isBlank() 
                ? info.getTitle() 
                : "Untitled";
        
        String isbn = null;
        if (info.getIndustryIdentifiers() != null) {
            isbn = info.getIndustryIdentifiers().stream()
                    .filter(id -> "ISBN_13".equals(id.getType()) || "ISBN_10".equals(id.getType()))
                    .map(GoogleBooksResponse.IndustryIdentifier::getIdentifier)
                    .findFirst()
                    .orElse(null);
        }
        
        String author = info.getAuthors() != null && !info.getAuthors().isEmpty() 
                ? String.join(", ", info.getAuthors()) 
                : "Unknown Author";
        
        String coverImage = null;
        if (info.getImageLinks() != null) {
            coverImage = info.getImageLinks().getThumbnail() != null 
                    ? info.getImageLinks().getThumbnail()
                    : info.getImageLinks().getSmallThumbnail();
        }
        
        return BookDTO.builder()
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
}

