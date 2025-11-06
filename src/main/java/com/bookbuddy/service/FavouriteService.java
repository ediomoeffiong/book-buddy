package com.bookbuddy.service;

import com.bookbuddy.dto.BookDTO;
import com.bookbuddy.exception.DuplicateResourceException;
import com.bookbuddy.exception.ResourceNotFoundException;
import com.bookbuddy.model.Book;
import com.bookbuddy.model.Favourite;
import com.bookbuddy.model.User;
import com.bookbuddy.repository.BookRepository;
import com.bookbuddy.repository.FavouriteRepository;
import com.bookbuddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FavouriteService {
    
    private final FavouriteRepository favouriteRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    
    public void addToFavourites(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        
        if (favouriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new DuplicateResourceException("Book is already in your favourites");
        }
        
        Favourite favourite = Favourite.builder()
                .user(user)
                .book(book)
                .build();
        
        favouriteRepository.save(favourite);
    }
    
    public void removeFromFavourites(Long userId, Long bookId) {
        if (!favouriteRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ResourceNotFoundException("Book not found in your favourites");
        }
        favouriteRepository.deleteByUserIdAndBookId(userId, bookId);
    }
    
    public List<BookDTO> getFavouriteBooks(Long userId) {
        return favouriteRepository.findByUserId(userId).stream()
                .map(favourite -> bookService.getBookById(favourite.getBook().getId()))
                .collect(Collectors.toList());
    }
    
    public Page<BookDTO> getFavouriteBooks(Long userId, Pageable pageable) {
        return favouriteRepository.findByUserId(userId, pageable)
                .map(favourite -> bookService.getBookById(favourite.getBook().getId()));
    }
    
    public boolean isFavourite(Long userId, Long bookId) {
        return favouriteRepository.existsByUserIdAndBookId(userId, bookId);
    }
}

