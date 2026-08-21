package com.dennymathew.streamhub.catalog;

import com.dennymathew.streamhub.catalog.dto.MovieResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CatalogClient {

    private final RestClient restClient;
    public CatalogClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    public MovieResponse getMovie(Long id) {
        return restClient.get()
                .uri("/movies/{id}", id)
                .retrieve()
                .body(MovieResponse.class);
    }

    public List<MovieResponse> getMoviesByIds(List<Long> ids) {
        return restClient.get()
                .uri(uri -> uri.path("/movies/batch")
                        .queryParam("ids", ids)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<MovieResponse>>() {});
    }

}
