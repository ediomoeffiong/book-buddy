package com.bookbuddy.controller;

import com.bookbuddy.dto.AddBookToShelfRequest;
import com.bookbuddy.dto.UpdateProgressRequest;
import com.bookbuddy.dto.UserBookDTO;
import com.bookbuddy.model.UserBook.ShelfType;
import com.bookbuddy.service.UserBookService;
import com.bookbuddy.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shelves")
@RequiredArgsConstructor
public class ShelfController {

    private final UserBookService userBookService;
    private final AuthUtil authUtil;
    
    @PostMapping("/add")
    public ResponseEntity<UserBookDTO> addBookToShelf(
            @Valid @RequestBody AddBookToShelfRequest request,
            Authentication authentication) {

        Long userId = getUserIdFromAuth(authentication);
        UserBookDTO userBook = userBookService.addBookToShelf(
                userId, request.getBookId(), request.getShelf(), request.getNotes());
        return new ResponseEntity<>(userBook, HttpStatus.CREATED);
    }
    
    @PutMapping("/move/{bookId}")
    public ResponseEntity<UserBookDTO> moveBookToShelf(
            @PathVariable Long bookId,
            @RequestParam ShelfType shelf,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        UserBookDTO userBook = userBookService.moveBookToShelf(userId, bookId, shelf);
        return ResponseEntity.ok(userBook);
    }
    
    @DeleteMapping("/remove/{bookId}")
    public ResponseEntity<Void> removeBookFromLibrary(
            @PathVariable Long bookId,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        userBookService.removeBookFromLibrary(userId, bookId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{shelf}")
    public ResponseEntity<List<UserBookDTO>> getBooksByShelf(
            @PathVariable ShelfType shelf,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        List<UserBookDTO> books = userBookService.getBooksByShelf(userId, shelf);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/{shelf}/paginated")
    public ResponseEntity<Page<UserBookDTO>> getBooksByShelfPaginated(
            @PathVariable ShelfType shelf,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserBookDTO> books = userBookService.getBooksByShelf(userId, shelf, pageable);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/timeline")
    public ResponseEntity<Page<UserBookDTO>> getReadingTimeline(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserBookDTO> timeline = userBookService.getReadingTimeline(userId, pageable);
        return ResponseEntity.ok(timeline);
    }
    
    @GetMapping("/currently-reading")
    public ResponseEntity<List<UserBookDTO>> getCurrentlyReading(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        List<UserBookDTO> books = userBookService.getCurrentlyReading(userId);
        return ResponseEntity.ok(books);
    }
    
    @PutMapping("/progress/{bookId}")
    public ResponseEntity<UserBookDTO> updateReadingProgress(
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateProgressRequest request,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        UserBookDTO userBook = userBookService.updateReadingProgress(
                userId, bookId, request.getCurrentPage(), request.getProgressPercentage());
        return ResponseEntity.ok(userBook);
    }
    
    @PutMapping("/rate/{bookId}")
    public ResponseEntity<UserBookDTO> rateBook(
            @PathVariable Long bookId,
            @RequestParam Integer rating,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        UserBookDTO userBook = userBookService.rateBook(userId, bookId, rating);
        return ResponseEntity.ok(userBook);
    }
    
    private Long getUserIdFromAuth(Authentication authentication) {
        return authUtil.getUserIdFromAuthentication(authentication);
    }
}

