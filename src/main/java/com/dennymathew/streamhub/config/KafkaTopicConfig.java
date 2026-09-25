package com.dennymathew.streamhub.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.Optional;
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
    @ConditionalOnProperty(name = "spring.kafka.admin.auto-create", havingValue = "true", matchIfMissing = true)
    NewTopic movieWatchedTopic(@Value("${streamhub.kafka.movie-watched-topic}") String topic,
                              @Value("${streamhub.kafka.topic-partitions:3}") int partitions,
                              @Value("${streamhub.kafka.topic-replicas:1}") short replicas) {
        return topic(topic, partitions, replicas);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.admin.auto-create", havingValue = "true", matchIfMissing = true)
    NewTopic movieWatchedDltTopic(@Value("${streamhub.kafka.movie-watched-topic}") String topic,
                                 @Value("${streamhub.kafka.topic-partitions:3}") int partitions,
                                 @Value("${streamhub.kafka.topic-replicas:1}") short replicas) {
        return topic(topic + ".DLT", partitions, replicas);
    }

    private NewTopic topic(String name, int partitions, short replicas) {
        if (partitions < 1 || (replicas < 1 && replicas != -1)) {
            throw new IllegalArgumentException("Kafka partitions must be positive; replicas must be positive or -1 for broker default");
        }
        return new NewTopic(name, Optional.of(partitions), replicas == -1 ? Optional.empty() : Optional.of(replicas));
    }

    @Bean
    CommonErrorHandler movieWatchedErrorHandler(KafkaOperations<Object, Object> kafkaOperations) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaOperations,
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
        // Do not acknowledge a failed record unless its dead-letter publication succeeded.
        recoverer.setFailIfSendResultIsError(true);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(500L, 2L));
        handler.setCommitRecovered(true);
        return handler;
    }
}
