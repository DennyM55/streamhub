package com.dennymathew.streamhub.events;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieWatchedOutboxPublisherTest {
    @Test void brokerAcknowledgementMarksDelivered() throws Exception {
        var repo=mock(MovieWatchedOutboxRepository.class);
        var producer=mock(MovieWatchedEventProducer.class);
        var row=new MovieWatchedOutbox(1L,2L,LocalDateTime.now());
        when(repo.findTop25ByPublishedAtIsNullOrderByCreatedAtAsc()).thenReturn(List.of(row));
        new MovieWatchedOutboxPublisher(repo,producer).publishPending();
        verify(producer).publish(row.event());
        verify(repo).save(row);
        assertThat(row.getPublishedAt()).isNotNull();
    }
    @Test void failedDeliveryRemainsPendingWithStableEventId() throws Exception {
        var repo=mock(MovieWatchedOutboxRepository.class);
        var producer=mock(MovieWatchedEventProducer.class);
        var row=new MovieWatchedOutbox(1L,2L,LocalDateTime.now());
        var eventId=row.event().eventId();
        when(repo.findTop25ByPublishedAtIsNullOrderByCreatedAtAsc()).thenReturn(List.of(row));
        doThrow(new java.util.concurrent.TimeoutException()).when(producer).publish(any());
        var publisher=new MovieWatchedOutboxPublisher(repo,producer);
        publisher.publishPending();publisher.publishPending();
        verify(producer,times(2)).publish(row.event());
        verify(repo,never()).save(any());
        assertThat(row.getPublishedAt()).isNull();
        assertThat(row.event().eventId()).isEqualTo(eventId);
    }
}
