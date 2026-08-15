package com.dennymathew.catalog.movie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateMovieRequest(
        @NotBlank
        String title,

        String description,

        @NotBlank
        String genre,

        @NotNull
        Integer releaseYear,

        @NotNull
        @Positive
        Integer durationMinutes,

        String thumbnailUrl,

        String mediaUrl
) {
}
