package com.bookbuddy.repository;

import com.bookbuddy.model.Favourite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    
    Optional<Favourite> findByUserIdAndBookId(Long userId, Long bookId);
    
    List<Favourite> findByUserId(Long userId);
    
    Page<Favourite> findByUserId(Long userId, Pageable pageable);
    
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    
    void deleteByUserIdAndBookId(Long userId, Long bookId);
}

