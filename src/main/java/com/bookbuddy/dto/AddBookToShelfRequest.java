package com.bookbuddy.dto;

import com.bookbuddy.model.UserBook.ShelfType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBookToShelfRequest {

    @NotNull(message = "Book ID is required")
    private Long bookId;

    @NotNull(message = "Shelf type is required")
    private ShelfType shelf;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}

