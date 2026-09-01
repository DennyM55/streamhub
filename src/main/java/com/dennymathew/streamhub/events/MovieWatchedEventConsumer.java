package com.dennymathew.streamhub.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MovieWatchedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MovieWatchedEventConsumer.class);

    private final ProcessedMovieWatchedEventRepository processedEvents;

    public MovieWatchedEventConsumer(ProcessedMovieWatchedEventRepository processedEvents) {
        this.processedEvents = processedEvents;
    }

    @KafkaListener(topics = "${streamhub.kafka.movie-watched-topic}", groupId = "streamhub-history")
    public void handle(MovieWatchedEvent event) {
        if (!markProcessed(event)) {
            log.info("Skipping duplicate movie watched event: {}", event.eventId());
            return;
        }
        log.info("Received movie watched event: {}", event);
    }

    private boolean markProcessed(MovieWatchedEvent event) {
        return processedEvents.insertIfAbsent(event.eventId(), Instant.now()) == 1;
    }
}
