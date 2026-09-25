package com.dennymathew.streamhub.catalog;

import com.dennymathew.streamhub.catalog.dto.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class CatalogClient {
    private final RestClient restClient;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    CatalogClient(String catalogUrl, String catalogKey) {
        this(catalogUrl, catalogKey, 1000, 3000);
    }

    @Autowired
    public CatalogClient(@Value("${streamhub.catalog.url}") String catalogUrl,
                         @Value("${streamhub.catalog.key:}") String catalogKey,
                         @Value("${streamhub.catalog.connect-timeout-ms:1000}") int connectTimeout,
                         @Value("${streamhub.catalog.read-timeout-ms:3000}") int readTimeout) {
        if (connectTimeout < 1 || readTimeout < 1) {
            throw new IllegalArgumentException("Catalogue timeouts must be positive");
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        restClient = RestClient.builder().baseUrl(catalogUrl)
                .defaultHeader("X-Catalog-Key", catalogKey).requestFactory(factory).build();
        retry = Retry.of("catalog", RetryConfig.custom().maxAttempts(3)
                .waitDuration(Duration.ofMillis(200))
                .retryExceptions(ResourceAccessException.class, HttpServerErrorException.class).build());
        circuitBreaker = CircuitBreaker.of("catalog", CircuitBreakerConfig.custom()
                .slidingWindowSize(5).minimumNumberOfCalls(5).failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .recordExceptions(ResourceAccessException.class, HttpServerErrorException.class)
                .ignoreExceptions(HttpClientErrorException.class).build());
    }

    // Retry only reads: automatically retrying POST could create duplicate movies.
    private <T> T read(Supplier<T> action) {
        return circuitBreaker.executeSupplier(() -> retry.executeSupplier(action));
    }

    public MovieResponse getMovie(Long id) {
        return read(() -> restClient.get().uri("/movies/{id}", id).retrieve().body(MovieResponse.class));
    }

    public MoviePage getMovies(String search, String genre, Integer releaseYear,
                               Integer durationMinutes, int page, int size, String sort) {
        return read(() -> restClient.get().uri(uri -> uri.path("/movies")
                .queryParamIfPresent("search", Optional.ofNullable(search))
                .queryParamIfPresent("genre", Optional.ofNullable(genre))
                .queryParamIfPresent("releaseYear", Optional.ofNullable(releaseYear))
                .queryParamIfPresent("durationMinutes", Optional.ofNullable(durationMinutes))
                .queryParam("page", page).queryParam("size", size).queryParam("sort", sort).build())
                .retrieve().body(MoviePage.class));
    }

    public List<String> getGenres() {
        return read(() -> restClient.get().uri("/movies/genres").retrieve()
                .body(new ParameterizedTypeReference<List<String>>() {}));
    }

    public MovieResponse createMovie(CreateMovieRequest request) {
        return restClient.post().uri("/movies").body(request).retrieve().body(MovieResponse.class);
    }

    public MovieResponse updateMovie(Long id, UpdateMovieRequest request) {
        return restClient.put().uri("/movies/{id}", id).body(request).retrieve().body(MovieResponse.class);
    }

    public void deleteMovie(Long id) {
        restClient.delete().uri("/movies/{id}", id).retrieve().toBodilessEntity();
    }

    public List<MovieResponse> getMoviesByIds(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return read(() -> restClient.get().uri(uri -> uri.path("/movies/batch").queryParam("ids", ids).build())
                .retrieve().body(new ParameterizedTypeReference<List<MovieResponse>>() {}));
    }
}
