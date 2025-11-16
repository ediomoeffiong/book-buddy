package com.bookbuddy.controller;

import com.bookbuddy.dto.BookDTO;
import com.bookbuddy.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    
    private final BookService bookService;
    
    @GetMapping
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<BookDTO>> searchBooksInDatabase(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = bookService.searchBooksInDatabase(query, pageable);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/search/external")
    public ResponseEntity<List<BookDTO>> searchBooksFromExternalAPI(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int maxResults) {
        
        List<BookDTO> books = bookService.searchBooksFromExternalAPI(query, maxResults);
        return ResponseEntity.ok(books);
    }
    
    @PostMapping("/import/{googleBooksId}")
    public ResponseEntity<BookDTO> importBookFromExternalAPI(@PathVariable String googleBooksId) {
        BookDTO book = bookService.saveBookFromExternalAPI(googleBooksId);
        return ResponseEntity.ok(book);
    }

    @PostMapping("/import/top")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookDTO>> importTopFromExternalAPI(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int maxResults) {

        List<BookDTO> books = bookService.saveTopFromExternalAPI(query, maxResults);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<BookDTO>> getBooksByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = bookService.getBooksByCategory(category, pageable);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/author/{author}")
    public ResponseEntity<Page<BookDTO>> getBooksByAuthor(
            @PathVariable String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = bookService.getBooksByAuthor(author, pageable);
        return ResponseEntity.ok(books);
    }
}

