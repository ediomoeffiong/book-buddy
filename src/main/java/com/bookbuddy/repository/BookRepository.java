package com.bookbuddy.repository;

import com.bookbuddy.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    Optional<Book> findByIsbn(String isbn);
    
    Optional<Book> findByGoogleBooksId(String googleBooksId);
    
    Optional<Book> findByOpenLibraryId(String openLibraryId);
    
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> searchBooks(@Param("query") String query, Pageable pageable);
    
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
    
    @Query("SELECT b FROM Book b JOIN b.categories c WHERE LOWER(c) = LOWER(:category)")
    Page<Book> findByCategory(@Param("category") String category, Pageable pageable);
}

