package com.dennymathew.streamhub.events;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface MovieWatchedOutboxRepository extends JpaRepository<MovieWatchedOutbox, UUID> {
    List<MovieWatchedOutbox> findTop25ByPublishedAtIsNullOrderByCreatedAtAsc();
}
