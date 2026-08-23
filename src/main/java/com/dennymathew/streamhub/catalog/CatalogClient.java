package com.dennymathew.streamhub.catalog;

import com.dennymathew.streamhub.catalog.dto.MovieResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient() {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(500);
        factory.setReadTimeout(1000);

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081")
                .requestFactory(factory)
                .build();
    }

    public MovieResponse getMovie(Long id) {
        return restClient.get()
                .uri("/movies/{id}", id)
                .retrieve()
                .body(MovieResponse.class);
    }

    @Retry(name = "catalog")
    @CircuitBreaker(name = "catalog")
    public List<MovieResponse> getMoviesByIds(List<Long> ids) {
        return restClient.get()
                .uri(uri -> uri.path("/movies/batch")
                        .queryParam("ids", ids)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<MovieResponse>>() {
                });
    }

}
