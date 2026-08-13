package com.dennymathew.streamhub.config;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(
            RuntimeException exception,
            Cache cache,
            Object key) {

        System.err.println(
                "Redis GET failed for cache=" +
                        cache.getName() +
                        ", key=" + key +
                        ". Falling back to database."
        );
    }

    @Override
    public void handleCachePutError(
            RuntimeException exception,
            Cache cache,
            Object key,
            Object value) {

        System.err.println(
                "Redis PUT failed for cache=" +
                        cache.getName() +
                        ", key=" + key
        );
    }

    @Override
    public void handleCacheEvictError(
            RuntimeException exception,
            Cache cache,
            Object key) {

        System.err.println(
                "Redis EVICT failed for cache=" +
                        cache.getName() +
                        ", key=" + key
        );
    }

    @Override
    public void handleCacheClearError(
            RuntimeException exception,
            Cache cache) {

        System.err.println(
                "Redis CLEAR failed for cache=" +
                        cache.getName()
        );
    }
}