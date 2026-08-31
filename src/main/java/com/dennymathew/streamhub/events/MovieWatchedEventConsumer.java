package com.dennymathew.streamhub.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MovieWatchedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MovieWatchedEventConsumer.class);

    @KafkaListener(topics = "${streamhub.kafka.movie-watched-topic}")
    public void handle(MovieWatchedEvent event) {
        log.info("Received movie watched event: {}", event);
    }
}
