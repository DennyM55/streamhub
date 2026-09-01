package com.dennymathew.streamhub.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedMovieWatchedEventRepository
        extends JpaRepository<ProcessedMovieWatchedEvent, UUID> {

    @Modifying
    @Transactional
    @Query(value = """
            insert into processed_movie_watched_events (event_id, processed_at)
            values (:eventId, :processedAt)
            on conflict (event_id) do nothing
            """, nativeQuery = true)
    int insertIfAbsent(UUID eventId, Instant processedAt);
}
