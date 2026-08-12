package com.dennymathew.streamhub.catalog.dto;

public record MovieResponse(
        Long id,
        String title,
        String description,
        String genre,
        Integer releaseYear,
        Integer durationMinutes,
        String thumbnailUrl,
        String mediaUrl
) {
}
