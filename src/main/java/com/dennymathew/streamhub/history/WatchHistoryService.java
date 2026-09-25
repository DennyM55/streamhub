package com.dennymathew.streamhub.history;

import com.dennymathew.streamhub.catalog.CatalogClient;
import com.dennymathew.streamhub.catalog.dto.MovieResponse;
import com.dennymathew.streamhub.events.MovieWatchedOutbox;
import com.dennymathew.streamhub.events.MovieWatchedOutboxRepository;
import com.dennymathew.streamhub.history.dto.WatchHistoryResponse;
import com.dennymathew.streamhub.user.User;
import com.dennymathew.streamhub.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WatchHistoryService {
    private final WatchHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final CatalogClient catalogClient;
    private final MovieWatchedOutboxRepository outbox;

    public WatchHistoryService(WatchHistoryRepository historyRepository, UserRepository userRepository,
                               CatalogClient catalogClient, MovieWatchedOutboxRepository outbox) {
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.catalogClient = catalogClient;
        this.outbox = outbox;
    }

    @Transactional
    public WatchHistoryResponse saveProgressByEmail(String email, Long movieId, Integer progressSeconds) {
        if (progressSeconds == null || progressSeconds < 0) {
            throw new IllegalArgumentException("Progress must be zero or greater");
        }
        User user = findUser(email);
        MovieResponse movie = catalogClient.getMovie(movieId);
        if (movie.durationMinutes() != null && progressSeconds > movie.durationMinutes() * 60L) {
            throw new IllegalArgumentException("Progress exceeds movie duration");
        }
        WatchHistory history = historyRepository.findByUserIdAndMovieId(user.getId(), movieId)
                .orElseGet(() -> {
                    WatchHistory row = new WatchHistory();
                    row.setUser(user);
                    row.setMovieId(movieId);
                    return row;
                });
        history.setMovieTitle(movie.title());
        history.setProgressSeconds(progressSeconds);
        history.setLastWatchedAt(LocalDateTime.now());
        WatchHistory saved = historyRepository.save(history);
        // Persist in the same database transaction; Kafka outages cannot lose the event.
        outbox.save(new MovieWatchedOutbox(movieId, user.getId(), saved.getLastWatchedAt()));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WatchHistoryResponse> getHistoryByEmail(String email) {
        return historyRepository.findByUserIdOrderByLastWatchedAtDesc(findUser(email).getId())
                .stream().map(this::toResponse).toList();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private WatchHistoryResponse toResponse(WatchHistory history) {
        return new WatchHistoryResponse(history.getId(), history.getUser().getId(), history.getMovieId(),
                history.getMovieTitle(), history.getProgressSeconds(), history.getLastWatchedAt());
    }
}
