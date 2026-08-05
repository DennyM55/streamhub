package com.dennymathew.streamhub.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchHistoryRepository
        extends JpaRepository<WatchHistory, Long> {

    Optional<WatchHistory> findByUserIdAndMovieId(
            Long userId,
            Long movieId
    );

    List<WatchHistory> findByUserIdOrderByLastWatchedAtDesc(Long userId);
}
