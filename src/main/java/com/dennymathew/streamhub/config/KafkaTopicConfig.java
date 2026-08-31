package com.dennymathew.streamhub.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic movieWatchedTopic(@Value("${streamhub.kafka.movie-watched-topic}") String topic) {
        return new NewTopic(topic, 1, (short) 1);
    }
}
