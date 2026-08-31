package com.dennymathew.streamhub.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record MovieWatchedEvent(
        Long movieId,
        Long userId,
        UUID eventId,
        LocalDateTime watchedAt
) {
}
