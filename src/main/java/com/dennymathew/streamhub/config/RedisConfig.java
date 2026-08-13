package com.dennymathew.streamhub.config;

import com.dennymathew.streamhub.catalog.dto.MovieResponse;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;

@Configuration
public class RedisConfig implements CachingConfigurer {
    private final RedisCacheErrorHandler redisCacheErrorHandler;

    public RedisConfig(RedisCacheErrorHandler redisCacheErrorHandler) {
        this.redisCacheErrorHandler = redisCacheErrorHandler;
    }

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper) {

        RedisCacheConfiguration cacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                new JacksonJsonRedisSerializer<>(
                                                        objectMapper,
                                                        MovieResponse.class
                                                )
                                        )
                        ).entryTtl(Duration.ofMinutes(10)); // Set the TTL for cache entries

        return RedisCacheManager
                .builder(redisConnectionFactory)
                .withInitialCacheConfigurations(
                        Map.of("movies", cacheConfiguration)
                )
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return redisCacheErrorHandler;
    }
}
