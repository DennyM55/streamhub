package com.dennymathew.streamhub.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MovieWatchedEventProducer {

    private final KafkaTemplate<String, MovieWatchedEvent> kafkaTemplate;
    private final String topic;

    public MovieWatchedEventProducer(
            KafkaTemplate<String, MovieWatchedEvent> kafkaTemplate,
            @Value("${streamhub.kafka.movie-watched-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishMovieWatched(Long movieId, Long userId, LocalDateTime watchedAt) {
        MovieWatchedEvent event = new MovieWatchedEvent(
                movieId,
                userId,
                UUID.randomUUID(),
                watchedAt
        );

        kafkaTemplate.send(topic, movieId.toString(), event);
    }
}
