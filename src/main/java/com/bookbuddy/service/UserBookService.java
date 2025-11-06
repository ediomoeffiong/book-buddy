package com.bookbuddy.service;

import com.bookbuddy.dto.BookDTO;
import com.bookbuddy.dto.UserBookDTO;
import com.bookbuddy.exception.DuplicateResourceException;
import com.bookbuddy.exception.ResourceNotFoundException;
import com.bookbuddy.model.Book;
import com.bookbuddy.model.User;
import com.bookbuddy.model.UserBook;
import com.bookbuddy.model.UserBook.ShelfType;
import com.bookbuddy.repository.BookRepository;
import com.bookbuddy.repository.UserBookRepository;
import com.bookbuddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserBookService {
    
    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    
    public UserBookDTO addBookToShelf(Long userId, Long bookId, ShelfType shelf) {
        return addBookToShelf(userId, bookId, shelf, null);
    }

    public UserBookDTO addBookToShelf(Long userId, Long bookId, ShelfType shelf, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        // Check if book already exists in user's library
        if (userBookRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new DuplicateResourceException("Book already exists in your library");
        }

        UserBook userBook = UserBook.builder()
                .user(user)
                .book(book)
                .shelf(shelf)
                .notes(notes)
                .build();

        if (shelf == ShelfType.CURRENTLY_READING) {
            userBook.setStartedReadingAt(LocalDateTime.now());
        } else if (shelf == ShelfType.READ) {
            userBook.setFinishedReadingAt(LocalDateTime.now());
            userBook.setProgressPercentage(100.0);
        }

        userBook = userBookRepository.save(userBook);
        return convertToDTO(userBook);
    }
    
    public UserBookDTO moveBookToShelf(Long userId, Long bookId, ShelfType newShelf) {
        UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in your library"));
        
        ShelfType oldShelf = userBook.getShelf();
        userBook.setShelf(newShelf);
        
        // Update timestamps based on shelf change
        if (newShelf == ShelfType.CURRENTLY_READING && oldShelf != ShelfType.CURRENTLY_READING) {
            userBook.setStartedReadingAt(LocalDateTime.now());
        } else if (newShelf == ShelfType.READ && oldShelf != ShelfType.READ) {
            userBook.setFinishedReadingAt(LocalDateTime.now());
            userBook.setProgressPercentage(100.0);
            if (userBook.getBook().getPageCount() != null) {
                userBook.setCurrentPage(userBook.getBook().getPageCount());
            }
        }
        
        userBook = userBookRepository.save(userBook);
        return convertToDTO(userBook);
    }
    
    public void removeBookFromLibrary(Long userId, Long bookId) {
        if (!userBookRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ResourceNotFoundException("Book not found in your library");
        }
        userBookRepository.deleteByUserIdAndBookId(userId, bookId);
    }
    
    public UserBookDTO updateReadingProgress(Long userId, Long bookId, Integer currentPage, Double progressPercentage) {
        UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in your library"));
        
        if (currentPage != null) {
            userBook.setCurrentPage(currentPage);
            
            // Calculate percentage if page count is available
            if (userBook.getBook().getPageCount() != null && userBook.getBook().getPageCount() > 0) {
                double calculatedPercentage = (currentPage * 100.0) / userBook.getBook().getPageCount();
                userBook.setProgressPercentage(Math.min(calculatedPercentage, 100.0));
            }
        }
        
        if (progressPercentage != null) {
            userBook.setProgressPercentage(progressPercentage);
        }
        
        // Auto-move to "Read" shelf if completed
        if (userBook.getProgressPercentage() != null && userBook.getProgressPercentage() >= 100.0) {
            userBook.setShelf(ShelfType.READ);
            userBook.setFinishedReadingAt(LocalDateTime.now());
        }
        
        userBook = userBookRepository.save(userBook);
        return convertToDTO(userBook);
    }
    
    public UserBookDTO rateBook(Long userId, Long bookId, Integer rating) {
        UserBook userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in your library"));
        
        userBook.setRating(rating);
        userBook = userBookRepository.save(userBook);
        
        return convertToDTO(userBook);
    }
    
    public List<UserBookDTO> getBooksByShelf(Long userId, ShelfType shelf) {
        return userBookRepository.findByUserIdAndShelf(userId, shelf).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<UserBookDTO> getBooksByShelf(Long userId, ShelfType shelf, Pageable pageable) {
        return userBookRepository.findByUserIdAndShelf(userId, shelf, pageable)
                .map(this::convertToDTO);
    }
    
    public Page<UserBookDTO> getReadingTimeline(Long userId, Pageable pageable) {
        return userBookRepository.findRecentlyUpdatedByUserId(userId, pageable)
                .map(this::convertToDTO);
    }
    
    public List<UserBookDTO> getCurrentlyReading(Long userId) {
        return userBookRepository.findCurrentlyReadingByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private UserBookDTO convertToDTO(UserBook userBook) {
        BookDTO bookDTO = bookService.getBookById(userBook.getBook().getId());

        return UserBookDTO.builder()
                .id(userBook.getId())
                .book(bookDTO)
                .shelf(userBook.getShelf())
                .currentPage(userBook.getCurrentPage())
                .progressPercentage(userBook.getProgressPercentage())
                .startedReadingAt(userBook.getStartedReadingAt())
                .finishedReadingAt(userBook.getFinishedReadingAt())
                .rating(userBook.getRating())
                .notes(userBook.getNotes())
                .createdAt(userBook.getCreatedAt())
                .updatedAt(userBook.getUpdatedAt())
                .build();
    }
}

