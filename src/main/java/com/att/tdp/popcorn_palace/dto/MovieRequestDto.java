package com.att.tdp.popcorn_palace.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MovieRequestDto {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Genre is required")
    private String genre;

    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration cannot be negative")
    private Integer duration;

    // Rating can be null for new movies
    private Double rating;

    @PositiveOrZero(message = "Release year cannot be negative")
    @NotNull(message = "Release year is required")
    private Integer releaseYear;
}
