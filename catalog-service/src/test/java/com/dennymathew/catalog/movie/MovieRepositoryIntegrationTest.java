package com.dennymathew.catalog.movie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MovieRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MovieRepository movieRepository;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Test
    void savesMovieAndFindsItById() {
        Movie movie = new Movie();
        movie.setTitle("Inception");
        movie.setDescription("Dream heist");
        movie.setGenre("Sci-Fi");
        movie.setReleaseYear(2010);
        movie.setDurationMinutes(148);
        movie.setThumbnailUrl("https://cdn.example.com/inception.jpg");
        movie.setMediaUrl("https://media.example.com/inception.mp4");

        Movie savedMovie = movieRepository.save(movie);

        Optional<Movie> foundMovie = movieRepository.findById(savedMovie.getId());

        assertThat(foundMovie).isPresent();
        assertThat(foundMovie.get().getId()).isEqualTo(savedMovie.getId());
        assertThat(foundMovie.get().getTitle()).isEqualTo("Inception");
        assertThat(foundMovie.get().getDescription()).isEqualTo("Dream heist");
        assertThat(foundMovie.get().getGenre()).isEqualTo("Sci-Fi");
        assertThat(foundMovie.get().getReleaseYear()).isEqualTo(2010);
        assertThat(foundMovie.get().getDurationMinutes()).isEqualTo(148);
        assertThat(foundMovie.get().getThumbnailUrl()).isEqualTo("https://cdn.example.com/inception.jpg");
        assertThat(foundMovie.get().getMediaUrl()).isEqualTo("https://media.example.com/inception.mp4");
    }
}
