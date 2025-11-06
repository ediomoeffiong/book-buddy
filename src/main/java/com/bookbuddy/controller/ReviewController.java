package com.bookbuddy.controller;

import com.bookbuddy.dto.CreateReviewRequest;
import com.bookbuddy.dto.ReviewDTO;
import com.bookbuddy.service.ReviewService;
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

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthUtil authUtil;
    
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        ReviewDTO review = reviewService.createReview(
                userId, request.getBookId(), request.getContent(), request.getRating());
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }
    
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        ReviewDTO review = reviewService.updateReview(
                reviewId, userId, request.getContent(), request.getRating());
        return ResponseEntity.ok(review);
    }
    
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long reviewId) {
        ReviewDTO review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }
    
    @GetMapping("/book/{bookId}")
    public ResponseEntity<Page<ReviewDTO>> getReviewsForBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDTO> reviews = reviewService.getReviewsForBook(bookId, pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ReviewDTO>> getReviewsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDTO> reviews = reviewService.getReviewsByUser(userId, pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/my-reviews")
    public ResponseEntity<Page<ReviewDTO>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDTO> reviews = reviewService.getReviewsByUser(userId, pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/user/{userId}/book/{bookId}")
    public ResponseEntity<ReviewDTO> getUserReviewForBook(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        
        ReviewDTO review = reviewService.getUserReviewForBook(userId, bookId);
        return ResponseEntity.ok(review);
    }
    
    private Long getUserIdFromAuth(Authentication authentication) {
        return authUtil.getUserIdFromAuthentication(authentication);
    }
}

