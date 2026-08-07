package com.dennymathew.streamhub.history;

import com.dennymathew.streamhub.catalog.Movie;
import com.dennymathew.streamhub.catalog.MovieRepository;
import com.dennymathew.streamhub.history.dto.WatchHistoryResponse;
import com.dennymathew.streamhub.user.User;
import com.dennymathew.streamhub.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WatchHistoryService {

    private final WatchHistoryRepository watchHistoryRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public WatchHistoryService(
            WatchHistoryRepository watchHistoryRepository,
            UserRepository userRepository,
            MovieRepository movieRepository) {
        this.watchHistoryRepository = watchHistoryRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    public WatchHistoryResponse saveProgress(
            Long userId,
            Long movieId,
            Integer progressSeconds) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        WatchHistory history = watchHistoryRepository
                .findByUserIdAndMovieId(userId, movieId)
                .orElseGet(() -> {
                    WatchHistory newHistory = new WatchHistory();
                    newHistory.setUser(user);
                    newHistory.setMovie(movie);
                    return newHistory;
                });

        history.setProgressSeconds(progressSeconds);
        history.setLastWatchedAt(LocalDateTime.now());
        WatchHistory savedHistory = watchHistoryRepository.save(history);
        return new WatchHistoryResponse(
                savedHistory.getId(),
                savedHistory.getUser().getId(),
                savedHistory.getMovie().getId(),
                savedHistory.getMovie().getTitle(),
                savedHistory.getProgressSeconds(),
                savedHistory.getLastWatchedAt()
        );
    }
    public List<WatchHistoryResponse> getHistory(Long userId) {
        return watchHistoryRepository.findByUserIdOrderByLastWatchedAtDesc(userId)
                .stream()
                .map(history -> new WatchHistoryResponse(
                        history.getId(),
                        history.getUser().getId(),
                        history.getMovie().getId(),
                        history.getMovie().getTitle(),
                        history.getProgressSeconds(),
                        history.getLastWatchedAt()
                ))
                .toList();
    }

    public List<WatchHistoryResponse> getHistoryByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return watchHistoryRepository
                .findByUserIdOrderByLastWatchedAtDesc(user.getId())
                .stream()
                .map(history -> new WatchHistoryResponse(
                        history.getId(),
                        history.getUser().getId(),
                        history.getMovie().getId(),
                        history.getMovie().getTitle(),
                        history.getProgressSeconds(),
                        history.getLastWatchedAt()
                ))
                .toList();
    }
}