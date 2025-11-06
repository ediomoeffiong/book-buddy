package com.bookbuddy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDTO {
    
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private String publisher;
    private String publishedDate;
    private Integer pageCount;
    private String coverImageUrl;
    private List<String> categories;
    private String language;
    private String googleBooksId;
    private String openLibraryId;
    private Double averageRating;
    private Integer ratingsCount;
}

