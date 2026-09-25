package com.dennymathew.streamhub.events;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class MovieWatchedEventProducer {
    private final KafkaTemplate<String, MovieWatchedEvent> kafkaTemplate;
    private final String topic;
    public MovieWatchedEventProducer(KafkaTemplate<String, MovieWatchedEvent> kafkaTemplate,
                                    @Value("${streamhub.kafka.movie-watched-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }
    public void publish(MovieWatchedEvent event) throws Exception {
        // Wait for broker acknowledgement before marking the outbox row published.
        kafkaTemplate.send(topic, event.movieId().toString(), event).get(10, TimeUnit.SECONDS);
    }
}
