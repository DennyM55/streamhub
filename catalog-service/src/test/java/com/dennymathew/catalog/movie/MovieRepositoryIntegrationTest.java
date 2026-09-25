package com.dennymathew.catalog.movie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
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

    @Test
    void combinesCaseInsensitiveSearchGenreYearAndMaximumDuration() {
        saveFilm("Sintel", "Fantasy", 2010, 15);
        saveFilm("Sintel Extended", "Fantasy", 2010, 20);
        saveFilm("Sintel Documentary", "Documentary", 2010, 12);
        saveFilm("Sintel Returns", "Fantasy", 2020, 10);

        var movies = movieRepository.findAll(
                MovieSpecification.matching("SINTEL", "fantasy", 2010, 15), PageRequest.of(0, 10));

        assertThat(movies.getContent()).extracting(Movie::getTitle).containsExactly("Sintel");
    }

    @Test
    void searchTreatsSqlWildcardCharactersAsLiteralText() {
        saveFilm("100% Ready", "Comedy", 2020, 5);
        saveFilm("Ordinary Film", "Comedy", 2020, 5);

        var movies = movieRepository.findAll(
                MovieSpecification.matching("%", null, null, null), PageRequest.of(0, 10));

        assertThat(movies.getContent()).extracting(Movie::getTitle).containsExactly("100% Ready");
    }

    @Test
    void returnsDistinctSortedGenres() {
        saveFilm("One", "Fantasy", 2020, 5);
        saveFilm("Two", "Action", 2020, 5);
        saveFilm("Three", "Fantasy", 2020, 5);
        assertThat(movieRepository.findDistinctGenres()).containsExactly("Action", "Fantasy");
    }

    private void saveFilm(String title, String genre, int year, int minutes) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setGenre(genre);
        movie.setReleaseYear(year);
        movie.setDurationMinutes(minutes);
        movieRepository.save(movie);
    }
}
