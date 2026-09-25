package com.dennymathew.streamhub.events;
import org.junit.jupiter.api.Test;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
class MovieWatchedSerializationTest {
    @Test void eventRoundTripsThroughKafkaJacksonSerializersWithTimestamp() {
        var event=new MovieWatchedEvent(1L,2L,UUID.randomUUID(),LocalDateTime.now());
        var headers=new RecordHeaders();
        try(var serializer=new JacksonJsonSerializer<MovieWatchedEvent>();
            var deserializer=new JacksonJsonDeserializer<>(MovieWatchedEvent.class)) {
            byte[] bytes=serializer.serialize("movie-watched",headers,event);
            assertThat(deserializer.deserialize("movie-watched",headers,bytes)).isEqualTo(event);
        }
    }
}
