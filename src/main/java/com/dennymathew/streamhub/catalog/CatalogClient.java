package com.dennymathew.streamhub.catalog;

import com.dennymathew.streamhub.catalog.dto.MovieResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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


}