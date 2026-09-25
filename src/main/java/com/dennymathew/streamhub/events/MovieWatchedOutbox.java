package com.dennymathew.streamhub.events;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "movie_watched_outbox")
public class MovieWatchedOutbox {
    @Id private UUID eventId;
    @Column(nullable = false) private Long movieId;
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private LocalDateTime watchedAt;
    @Column(nullable = false) private Instant createdAt;
    private Instant publishedAt;
    protected MovieWatchedOutbox() {}
    public MovieWatchedOutbox(Long movieId, Long userId, LocalDateTime watchedAt) {
        this.eventId = UUID.randomUUID();
        this.movieId = movieId;
        this.userId = userId;
        this.watchedAt = watchedAt;
        this.createdAt = Instant.now();
    }
    public MovieWatchedEvent event() { return new MovieWatchedEvent(movieId, userId, eventId, watchedAt); }
    public void markPublished() { this.publishedAt = Instant.now(); }
    public Instant getPublishedAt() { return publishedAt; }
}
