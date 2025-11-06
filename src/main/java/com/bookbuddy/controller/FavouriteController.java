package com.bookbuddy.controller;

import com.bookbuddy.dto.BookDTO;
import com.bookbuddy.service.FavouriteService;
import com.bookbuddy.util.AuthUtil;
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
@RequestMapping("/api/favourites")
@RequiredArgsConstructor
public class FavouriteController {

    private final FavouriteService favouriteService;
    private final AuthUtil authUtil;
    
    @PostMapping("/{bookId}")
    public ResponseEntity<Void> addToFavourites(
            @PathVariable Long bookId,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        favouriteService.addToFavourites(userId, bookId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> removeFromFavourites(
            @PathVariable Long bookId,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        favouriteService.removeFromFavourites(userId, bookId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    public ResponseEntity<List<BookDTO>> getFavouriteBooks(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        List<BookDTO> books = favouriteService.getFavouriteBooks(userId);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<Page<BookDTO>> getFavouriteBooksPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDTO> books = favouriteService.getFavouriteBooks(userId, pageable);
        return ResponseEntity.ok(books);
    }
    
    @GetMapping("/check/{bookId}")
    public ResponseEntity<Boolean> isFavourite(
            @PathVariable Long bookId,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        boolean isFavourite = favouriteService.isFavourite(userId, bookId);
        return ResponseEntity.ok(isFavourite);
    }
    
    private Long getUserIdFromAuth(Authentication authentication) {
        return authUtil.getUserIdFromAuthentication(authentication);
    }
}

