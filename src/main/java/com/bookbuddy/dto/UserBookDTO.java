package com.bookbuddy.dto;

import com.bookbuddy.model.UserBook.ShelfType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBookDTO {

    private Long id;
    private BookDTO book;
    private ShelfType shelf;
    private Integer currentPage;
    private Double progressPercentage;
    private LocalDateTime startedReadingAt;
    private LocalDateTime finishedReadingAt;
    private Integer rating;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

