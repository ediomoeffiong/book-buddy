package com.bookbuddy.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {
    
    @NotNull(message = "Book ID is required")
    private Long bookId;
    
    @NotBlank(message = "Review content is required")
    @Size(min = 10, max = 5000, message = "Review must be between 10 and 5000 characters")
    private String content;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;
}

