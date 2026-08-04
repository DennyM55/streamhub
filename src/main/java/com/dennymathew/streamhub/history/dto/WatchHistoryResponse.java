package com.dennymathew.streamhub.history.dto;

import java.time.LocalDateTime;

public record WatchHistoryResponse(
        Long id,
        Long userId,
        Long movieId,
        String movieTitle,
        Integer progressSeconds,
        LocalDateTime lastWatchedAt
) {
}
