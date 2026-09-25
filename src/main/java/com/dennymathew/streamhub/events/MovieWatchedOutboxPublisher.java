package com.dennymathew.streamhub.events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MovieWatchedOutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(MovieWatchedOutboxPublisher.class);
    private final MovieWatchedOutboxRepository outbox;
    private final MovieWatchedEventProducer producer;
    public MovieWatchedOutboxPublisher(MovieWatchedOutboxRepository outbox, MovieWatchedEventProducer producer) {
        this.outbox = outbox;
        this.producer = producer;
    }
    @Scheduled(fixedDelayString = "${streamhub.kafka.outbox-delay-ms:2000}")
    public void publishPending() {
        for (MovieWatchedOutbox row : outbox.findTop25ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                producer.publish(row.event());
                row.markPublished();
                outbox.save(row);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                // The row remains pending; the next run retries using the same eventId.
                log.warn("Movie event delivery pending: {}", ex.getClass().getSimpleName());
                return;
            }
        }
    }
}
