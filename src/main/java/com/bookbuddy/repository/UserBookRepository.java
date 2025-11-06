package com.bookbuddy.repository;

import com.bookbuddy.model.UserBook;
import com.bookbuddy.model.UserBook.ShelfType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    
    Optional<UserBook> findByUserIdAndBookId(Long userId, Long bookId);
    
    List<UserBook> findByUserIdAndShelf(Long userId, ShelfType shelf);
    
    Page<UserBook> findByUserIdAndShelf(Long userId, ShelfType shelf, Pageable pageable);
    
    List<UserBook> findByUserId(Long userId);
    
    @Query("SELECT ub FROM UserBook ub WHERE ub.user.id = :userId " +
           "ORDER BY ub.updatedAt DESC")
    Page<UserBook> findRecentlyUpdatedByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT ub FROM UserBook ub WHERE ub.user.id = :userId " +
           "AND ub.shelf = 'CURRENTLY_READING' " +
           "ORDER BY ub.updatedAt DESC")
    List<UserBook> findCurrentlyReadingByUserId(@Param("userId") Long userId);
    
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    
    void deleteByUserIdAndBookId(Long userId, Long bookId);
}

