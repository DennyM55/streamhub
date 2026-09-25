package com.dennymathew.streamhub.events;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
class MovieWatchedEventConsumerTest {
    @Test void duplicateEventDoesNotThrowOrInsertANewIdentity() {
        var repo=mock(ProcessedMovieWatchedEventRepository.class);
        var event=new MovieWatchedEvent(1L,2L,UUID.randomUUID(),LocalDateTime.now());
        when(repo.insertIfAbsent(eq(event.eventId()),any())).thenReturn(1,0);
        var consumer=new MovieWatchedEventConsumer(repo);
        consumer.handle(event); consumer.handle(event);
        verify(repo,times(2)).insertIfAbsent(eq(event.eventId()),any());
        verifyNoMoreInteractions(repo);
    }
    @Test void malformedEventFailsBeforeDeduplication() {
        var repo=mock(ProcessedMovieWatchedEventRepository.class);
        var consumer=new MovieWatchedEventConsumer(repo);
        assertThatThrownBy(() -> consumer.handle(new MovieWatchedEvent(null,2L,null,null)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repo);
    }
}
