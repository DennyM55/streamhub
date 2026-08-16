package com.dennymathew.catalog.config;

import com.dennymathew.catalog.movie.dto.MovieResponse;
import org.springframework.cache.Cache;
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

import java.time.Duration;

@Configuration
public class RedisCacheConfig implements CachingConfigurer {

    private static final String MOVIES_CACHE = "movies";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration movieCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(MovieResponse.class)
                ));

        return RedisCacheManager.builder(redisConnectionFactory)
                .withCacheConfiguration(MOVIES_CACHE, movieCacheConfiguration)
                .build();
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                // PostgreSQL remains the source of truth when Redis is unavailable.
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                // Ignore cache write failures so API behavior is not tied to Redis availability.
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                // Ignore eviction failures; stale cache is preferable to failing the write path.
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                // No cache clear operation is used by the movie API.
            }
        };
    }
}
