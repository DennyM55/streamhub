package com.dennymathew.streamhub.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic movieWatchedTopic(@Value("${streamhub.kafka.movie-watched-topic}") String topic) {
        return new NewTopic(topic, 3, (short) 1);
    }

    @Bean
    NewTopic movieWatchedDltTopic(@Value("${streamhub.kafka.movie-watched-topic}") String topic) {
        return new NewTopic(topic + ".DLT", 3, (short) 1);
    }

    @Bean
    CommonErrorHandler movieWatchedErrorHandler(KafkaOperations<Object, Object> kafkaOperations) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaOperations,
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(0L, 2L));
    }
}
