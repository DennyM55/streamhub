package com.dennymathew.streamhub.history;

import com.dennymathew.streamhub.catalog.CatalogClient;
import com.dennymathew.streamhub.catalog.dto.MovieResponse;
import com.dennymathew.streamhub.events.*;
import com.dennymathew.streamhub.user.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchHistoryServiceTest {
    @Mock WatchHistoryRepository historyRepository;
    @Mock UserRepository users;
    @Mock CatalogClient catalog;
    @Mock MovieWatchedOutboxRepository outbox;
    @InjectMocks WatchHistoryService service;

    @Test void savesRemoteMovieProgressAndDurableEventTogether() {
        User user = new User(); user.setId(10L);
        when(users.findByEmail("demo@example.com")).thenReturn(Optional.of(user));
        when(catalog.getMovie(20L)).thenReturn(new MovieResponse(20L,"Sintel","Open film","Fantasy",2010,15,null,null));
        when(historyRepository.findByUserIdAndMovieId(10L,20L)).thenReturn(Optional.empty());
        when(historyRepository.save(any())).thenAnswer(inv -> { WatchHistory row=inv.getArgument(0);row.setId(30L);return row; });
        var result=service.saveProgressByEmail("demo@example.com",20L,120);
        ArgumentCaptor<MovieWatchedOutbox> event=ArgumentCaptor.forClass(MovieWatchedOutbox.class);
        verify(outbox).save(event.capture());
        assertThat(result.movieId()).isEqualTo(20L);
        assertThat(result.movieTitle()).isEqualTo("Sintel");
        assertThat(event.getValue().event().watchedAt()).isEqualTo(result.lastWatchedAt());
        assertThat(event.getValue().event().eventId()).isNotNull();
        assertThat(event.getValue().getPublishedAt()).isNull();
    }

    @Test void rejectsNegativeOrMissingProgressBeforeDatabaseAndNetworkWork() {
        assertThatThrownBy(() -> service.saveProgressByEmail("x",1L,-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.saveProgressByEmail("x",1L,null)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(users,catalog,historyRepository,outbox);
    }

    @Test void rejectsProgressPastMovieDurationWithoutWritingHistory() {
        User user = new User();user.setId(10L);
        when(users.findByEmail("x")).thenReturn(Optional.of(user));
        when(catalog.getMovie(20L)).thenReturn(new MovieResponse(20L,"Sintel","","Fantasy",2010,15,null,null));
        assertThatThrownBy(() -> service.saveProgressByEmail("x",20L,901)).hasMessageContaining("duration");
        verifyNoInteractions(historyRepository,outbox);
    }
}
