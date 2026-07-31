package com.dennymathew.streamhub.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMovieRequest(

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Genre is required")
        String genre,

        @NotNull(message = "Release year is required")
        @Min(value = 1888, message = "Release year is invalid")
        Integer releaseYear,

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be greater than 0")
        Integer durationMinutes,

        String thumbnailUrl,
        String mediaUrl
) {
}