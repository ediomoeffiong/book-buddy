package com.bookbuddy.service;

import com.bookbuddy.dto.BookDTO;
import com.bookbuddy.exception.ResourceNotFoundException;
import com.bookbuddy.model.Book;
import com.bookbuddy.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookService {
    
    private final BookRepository bookRepository;
    private final GoogleBooksService googleBooksService;
    
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return convertToDTO(book);
    }
    
    public BookDTO mapToDTO(Book book) {
        return convertToDTO(book);
    }
    
    public Page<BookDTO> searchBooksInDatabase(String query, Pageable pageable) {
        return bookRepository.searchBooks(query, pageable)
                .map(this::convertToDTO);
    }
    
    public List<BookDTO> searchBooksFromExternalAPI(String query, int maxResults) {
        return googleBooksService.searchBooks(query, maxResults);
    }
    
    public BookDTO saveBookFromExternalAPI(String googleBooksId) {
        // Check if book already exists
        Book existingBook = bookRepository.findByGoogleBooksId(googleBooksId).orElse(null);
        if (existingBook != null) {
            return convertToDTO(existingBook);
        }
        
        // Fetch from Google Books API
        BookDTO bookDTO = googleBooksService.getBookById(googleBooksId);
        if (bookDTO == null) {
            throw new ResourceNotFoundException("Book not found in Google Books API");
        }
        
        // Save to database
        Book book = convertToEntity(bookDTO);
        book = bookRepository.save(book);
        
        return convertToDTO(book);
    }
    
    public BookDTO createOrUpdateBook(BookDTO bookDTO) {
        Book book;
        
        if (bookDTO.getId() != null) {
            book = bookRepository.findById(bookDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
            updateBookFromDTO(book, bookDTO);
        } else {
            book = convertToEntity(bookDTO);
        }
        
        book = bookRepository.save(book);
        return convertToDTO(book);
    }
    
    public void updateBookRating(Long bookId, Double averageRating, Integer ratingsCount) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        book.setAverageRating(averageRating);
        book.setRatingsCount(ratingsCount);
        bookRepository.save(book);
    }
    
    public Page<BookDTO> getBooksByCategory(String category, Pageable pageable) {
        return bookRepository.findByCategory(category, pageable)
                .map(this::convertToDTO);
    }
    
    public Page<BookDTO> getBooksByAuthor(String author, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCase(author, pageable)
                .map(this::convertToDTO);
    }

    public Page<BookDTO> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    private BookDTO convertToDTO(Book book) {
        List<String> categories = sanitizeCategories(book.getCategories());
        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .publisher(book.getPublisher())
                .publishedDate(book.getPublishedDate())
                .pageCount(book.getPageCount())
                .coverImageUrl(book.getCoverImageUrl())
                .categories(categories)
                .language(book.getLanguage())
                .googleBooksId(book.getGoogleBooksId())
                .openLibraryId(book.getOpenLibraryId())
                .averageRating(book.getAverageRating() != null ? book.getAverageRating() : 0.0)
                .ratingsCount(book.getRatingsCount() != null ? book.getRatingsCount() : 0)
                .build();
    }
    
    private Book convertToEntity(BookDTO dto) {
        // Ensure required fields are not null
        String title = dto.getTitle() != null && !dto.getTitle().isBlank() 
                ? dto.getTitle() 
                : "Untitled";
        String author = dto.getAuthor() != null && !dto.getAuthor().isBlank() 
                ? dto.getAuthor() 
                : "Unknown Author";
        
        Book.BookBuilder builder = Book.builder()
                .title(title)
                .author(author)
                .isbn(dto.getIsbn())
                .description(dto.getDescription())
                .publisher(dto.getPublisher())
                .publishedDate(dto.getPublishedDate())
                .pageCount(dto.getPageCount())
                .coverImageUrl(dto.getCoverImageUrl())
                .categories(sanitizeCategories(dto.getCategories()))
                .language(dto.getLanguage())
                .googleBooksId(dto.getGoogleBooksId())
                .openLibraryId(dto.getOpenLibraryId())
                .averageRating(dto.getAverageRating() != null ? dto.getAverageRating() : 0.0)
                .ratingsCount(dto.getRatingsCount() != null ? dto.getRatingsCount() : 0);
        
        if (dto.getId() != null) {
            builder.id(dto.getId());
        }
        
        return builder.build();
    }
    
    private void updateBookFromDTO(Book book, BookDTO dto) {
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setDescription(dto.getDescription());
        book.setPublisher(dto.getPublisher());
        book.setPublishedDate(dto.getPublishedDate());
        book.setPageCount(dto.getPageCount());
        book.setCoverImageUrl(dto.getCoverImageUrl());
        book.setCategories(sanitizeCategories(dto.getCategories()));
        book.setLanguage(dto.getLanguage());
        book.setGoogleBooksId(dto.getGoogleBooksId());
        book.setOpenLibraryId(dto.getOpenLibraryId());
        if (dto.getAverageRating() != null) {
            book.setAverageRating(dto.getAverageRating());
        }
        if (dto.getRatingsCount() != null) {
            book.setRatingsCount(dto.getRatingsCount());
        }
    }
    
    private List<String> sanitizeCategories(List<String> categories) {
        if (categories == null) {
            return new ArrayList<>();
        }
        return categories.stream()
                .filter(category -> category != null && !category.isBlank())
                .map(String::trim)
                .collect(Collectors.toList());
    }
}

