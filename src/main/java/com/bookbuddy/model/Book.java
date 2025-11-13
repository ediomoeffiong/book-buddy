package com.bookbuddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    @Column(unique = true)
    private String isbn;
    
    @Column(length = 2000)
    private String description;
    
    private String publisher;
    
    private String publishedDate;
    
    private Integer pageCount;
    
    private String coverImageUrl;
    
    @ElementCollection
    @CollectionTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "category")
    @Builder.Default
    private List<String> categories = new ArrayList<>();
    
    private String language;
    
    // External API identifiers
    private String googleBooksId;
    
    private String openLibraryId;
    
    @Column(nullable = false)
    @Builder.Default
    private Double averageRating = 0.0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer ratingsCount = 0;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserBook> userBooks = new ArrayList<>();
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Favourite> favourites = new ArrayList<>();
}