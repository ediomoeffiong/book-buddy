package com.bookbuddy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProgressRequest {
    
    @Min(value = 0, message = "Current page must be at least 0")
    private Integer currentPage;
    
    @Min(value = 0, message = "Progress percentage must be at least 0")
    @Max(value = 100, message = "Progress percentage cannot exceed 100")
    private Double progressPercentage;
}

