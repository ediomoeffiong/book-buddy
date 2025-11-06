package com.bookbuddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_books", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "book_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShelfType shelf;
    
    // Reading Progress
    private Integer currentPage;
    
    private Double progressPercentage;
    
    private LocalDateTime startedReadingAt;
    
    private LocalDateTime finishedReadingAt;
    
    // Rating (1-5 stars)
    @Column(columnDefinition = "integer check (rating >= 1 and rating <= 5)")
    private Integer rating;

    // Optional notes (e.g., "Borrowed from friend")
    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum ShelfType {
        WANT_TO_READ,
        CURRENTLY_READING,
        READ
    }
}

