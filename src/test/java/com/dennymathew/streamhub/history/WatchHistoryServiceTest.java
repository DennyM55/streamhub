package com.dennymathew.streamhub.history;

import com.dennymathew.streamhub.catalog.Movie;
import com.dennymathew.streamhub.catalog.MovieRepository;
import com.dennymathew.streamhub.events.MovieWatchedEventProducer;
import com.dennymathew.streamhub.history.dto.WatchHistoryResponse;
import com.dennymathew.streamhub.user.User;
import com.dennymathew.streamhub.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WatchHistoryServiceTest {

    @Mock
    private WatchHistoryRepository watchHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieWatchedEventProducer movieWatchedEventProducer;

    @InjectMocks
    private WatchHistoryService watchHistoryService;

    @Test
    void saveProgressPublishesMovieWatchedEventAfterSavingHistory() {
        User user = new User();
        user.setId(10L);

        Movie movie = new Movie();
        movie.setId(20L);
        movie.setTitle("Inception");

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(movieRepository.findById(20L)).thenReturn(Optional.of(movie));
        when(watchHistoryRepository.findByUserIdAndMovieId(10L, 20L)).thenReturn(Optional.empty());
        when(watchHistoryRepository.save(org.mockito.ArgumentMatchers.any(WatchHistory.class)))
                .thenAnswer(invocation -> {
                    WatchHistory history = invocation.getArgument(0);
                    history.setId(30L);
                    return history;
                });

        WatchHistoryResponse response = watchHistoryService.saveProgress(10L, 20L, 120);

        ArgumentCaptor<LocalDateTime> watchedAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(movieWatchedEventProducer).publishMovieWatched(
                org.mockito.ArgumentMatchers.eq(20L),
                org.mockito.ArgumentMatchers.eq(10L),
                watchedAtCaptor.capture()
        );
        assertThat(watchedAtCaptor.getValue()).isNotNull();
        assertThat(response.lastWatchedAt()).isEqualTo(watchedAtCaptor.getValue());
    }
}
