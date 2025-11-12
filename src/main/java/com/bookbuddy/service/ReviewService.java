package com.bookbuddy.service;

import com.bookbuddy.dto.BookDTO;
import com.bookbuddy.dto.ReviewDTO;
import com.bookbuddy.dto.UserDTO;
import com.bookbuddy.exception.DuplicateResourceException;
import com.bookbuddy.exception.ResourceNotFoundException;
import com.bookbuddy.exception.UnauthorizedException;
import com.bookbuddy.model.Book;
import com.bookbuddy.model.Review;
import com.bookbuddy.model.User;
import com.bookbuddy.repository.BookRepository;
import com.bookbuddy.repository.ReviewRepository;
import com.bookbuddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    
    public ReviewDTO createReview(Long userId, Long bookId, String content, Integer rating) {
        validateReview(content, rating);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        
        // Check if user already reviewed this book
        if (reviewRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new DuplicateResourceException("You have already reviewed this book");
        }
        
        Review review = Review.builder()
                .user(user)
                .book(book)
                .content(content)
                .rating(rating)
                .build();
        
        review = reviewRepository.save(review);
        
        // Update book's average rating
        updateBookRating(bookId);
        
        return convertToDTO(review);
    }
    
    public ReviewDTO updateReview(Long reviewId, Long userId, String content, Integer rating) {
        validateReview(content, rating);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        // Check if user owns this review
        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own reviews");
        }
        
        review.setContent(content);
        review.setRating(rating);
        review = reviewRepository.save(review);
        
        // Update book's average rating
        updateBookRating(review.getBook().getId());
        
        return convertToDTO(review);
    }
    
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        // Check if user owns this review
        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }
        
        Long bookId = review.getBook().getId();
        reviewRepository.delete(review);
        
        // Update book's average rating
        updateBookRating(bookId);
    }
    
    public ReviewDTO getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        return convertToDTO(review);
    }
    
    public Page<ReviewDTO> getReviewsForBook(Long bookId, Pageable pageable) {
        return reviewRepository.findByBookId(bookId, pageable)
                .map(this::convertToDTO);
    }
    
    public Page<ReviewDTO> getReviewsByUser(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable)
                .map(this::convertToDTO);
    }
    
    public ReviewDTO getUserReviewForBook(Long userId, Long bookId) {
        Review review = reviewRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        return convertToDTO(review);
    }
    
    private void updateBookRating(Long bookId) {
        Double averageRating = reviewRepository.calculateAverageRatingForBook(bookId);
        Integer ratingsCount = reviewRepository.countReviewsForBook(bookId);
        
        bookService.updateBookRating(bookId, 
                averageRating != null ? averageRating : 0.0, 
                ratingsCount != null ? ratingsCount : 0);
    }
    
    private ReviewDTO convertToDTO(Review review) {
        UserDTO userDTO = UserDTO.builder()
                .id(review.getUser().getId())
                .username(review.getUser().getUsername())
                .firstName(review.getUser().getFirstName())
                .lastName(review.getUser().getLastName())
                .profileImageUrl(review.getUser().getProfileImageUrl())
                .build();
        
        BookDTO bookDTO = bookService.mapToDTO(review.getBook());
        
        return ReviewDTO.builder()
                .id(review.getId())
                .user(userDTO)
                .book(bookDTO)
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
    
    private void validateReview(String content, Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Review content cannot be empty");
        }
    }
}

