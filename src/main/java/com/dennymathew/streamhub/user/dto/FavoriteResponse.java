package com.dennymathew.streamhub.user.dto;

public record FavoriteResponse(
        Long id,
        Long userId,
        Long movieId,
        String movieTitle
) {
}