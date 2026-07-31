package com.dennymathew.streamhub.catalog.dto;

public record UpdateMovieRequest(
        String title,
        String description,
        String genre,
        Integer releaseYear,
        Integer durationMinutes,
        String thumbnailUrl,
        String mediaUrl
) {
}