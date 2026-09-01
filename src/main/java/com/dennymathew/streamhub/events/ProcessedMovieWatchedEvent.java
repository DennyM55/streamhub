package com.dennymathew.streamhub.events;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_movie_watched_events")
public class ProcessedMovieWatchedEvent {

    @Id
    private UUID eventId;

    private Instant processedAt;

    protected ProcessedMovieWatchedEvent() {
    }

    public ProcessedMovieWatchedEvent(UUID eventId, Instant processedAt) {
        this.eventId = eventId;
        this.processedAt = processedAt;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
