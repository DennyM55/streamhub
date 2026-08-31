package com.dennymathew.catalog.movie;

import com.dennymathew.catalog.movie.dto.CreateMovieRequest;
import com.dennymathew.catalog.movie.dto.MovieResponse;
import com.dennymathew.catalog.movie.dto.UpdateMovieRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    @Test
    void getMovieReturnsMovieResponseWhenMovieExists() {
        Movie movie = movie(1L, "Inception", "Sci-Fi");
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        MovieResponse response = movieService.getMovie(1L);

        assertThat(response).isEqualTo(new MovieResponse(
                1L,
                "Inception",
                "Dream heist",
                "Sci-Fi",
                2010,
                148,
                "https://cdn.example.com/inception.jpg",
                "https://media.example.com/inception.mp4"
        ));
    }

    @Test
    void getMovieThrowsWhenMovieDoesNotExist() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovie(99L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessage("Movie not found: 99");
    }

    @Test
    void createMovieSavesMovieFromRequestAndReturnsResponse() {
        CreateMovieRequest request = new CreateMovieRequest(
                "Interstellar",
                "Space rescue",
                "Sci-Fi",
                2014,
                169,
                "https://cdn.example.com/interstellar.jpg",
                "https://media.example.com/interstellar.mp4"
        );

        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movie = invocation.getArgument(0);
            movie.setId(2L);
            return movie;
        });

        MovieResponse response = movieService.createMovie(request);

        assertThat(response).isEqualTo(new MovieResponse(
                2L,
                "Interstellar",
                "Space rescue",
                "Sci-Fi",
                2014,
                169,
                "https://cdn.example.com/interstellar.jpg",
                "https://media.example.com/interstellar.mp4"
        ));
    }

    @Test
    void getMoviesUsesSearchAndGenreWhenBothAreProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> movies = new PageImpl<>(List.of(movie(1L, "Inception", "Sci-Fi")));
        when(movieRepository.findByTitleContainingIgnoreCaseAndGenreIgnoreCase("dream", "Sci-Fi", pageable))
                .thenReturn(movies);

        Page<MovieResponse> response = movieService.getMovies("dream", "Sci-Fi", pageable);

        assertThat(response.getContent()).extracting(MovieResponse::title).containsExactly("Inception");
        verify(movieRepository).findByTitleContainingIgnoreCaseAndGenreIgnoreCase("dream", "Sci-Fi", pageable);
        verifyNoMoreInteractions(movieRepository);
    }

    @Test
    void getMoviesUsesSearchOnlyWhenGenreIsBlank() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> movies = new PageImpl<>(List.of(movie(1L, "Inception", "Sci-Fi")));
        when(movieRepository.findByTitleContainingIgnoreCase("dream", pageable)).thenReturn(movies);

        Page<MovieResponse> response = movieService.getMovies("dream", " ", pageable);

        assertThat(response.getContent()).extracting(MovieResponse::title).containsExactly("Inception");
        verify(movieRepository).findByTitleContainingIgnoreCase("dream", pageable);
        verifyNoMoreInteractions(movieRepository);
    }

    @Test
    void getMoviesUsesGenreOnlyWhenSearchIsBlank() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> movies = new PageImpl<>(List.of(movie(1L, "Inception", "Sci-Fi")));
        when(movieRepository.findByGenreIgnoreCase("Sci-Fi", pageable)).thenReturn(movies);

        Page<MovieResponse> response = movieService.getMovies("", "Sci-Fi", pageable);

        assertThat(response.getContent()).extracting(MovieResponse::title).containsExactly("Inception");
        verify(movieRepository).findByGenreIgnoreCase("Sci-Fi", pageable);
        verifyNoMoreInteractions(movieRepository);
    }

    @Test
    void getMoviesUsesFindAllWhenSearchAndGenreAreBlank() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> movies = new PageImpl<>(List.of(movie(1L, "Inception", "Sci-Fi")));
        when(movieRepository.findAll(pageable)).thenReturn(movies);

        Page<MovieResponse> response = movieService.getMovies(null, null, pageable);

        assertThat(response.getContent()).extracting(MovieResponse::title).containsExactly("Inception");
        verify(movieRepository).findAll(pageable);
        verifyNoMoreInteractions(movieRepository);
    }

    @Test
    void updateMovieMutatesExistingMovieAndReturnsResponse() {
        Movie movie = movie(1L, "Inception", "Sci-Fi");
        UpdateMovieRequest request = new UpdateMovieRequest(
                "The Dark Knight",
                "Gotham crime drama",
                "Action",
                2008,
                152,
                "https://cdn.example.com/dark-knight.jpg",
                "https://media.example.com/dark-knight.mp4"
        );
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        MovieResponse response = movieService.updateMovie(1L, request);

        assertThat(response).isEqualTo(new MovieResponse(
                1L,
                "The Dark Knight",
                "Gotham crime drama",
                "Action",
                2008,
                152,
                "https://cdn.example.com/dark-knight.jpg",
                "https://media.example.com/dark-knight.mp4"
        ));
        assertThat(movie.getTitle()).isEqualTo("The Dark Knight");
        assertThat(movie.getGenre()).isEqualTo("Action");
    }

    @Test
    void updateMovieThrowsWhenMovieDoesNotExist() {
        UpdateMovieRequest request = new UpdateMovieRequest(
                "Unknown",
                "Missing",
                "Drama",
                2020,
                100,
                null,
                null
        );
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.updateMovie(99L, request))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessage("Movie not found: 99");
    }

    @Test
    void deleteMovieDeletesExistingMovie() {
        Movie movie = movie(1L, "Inception", "Sci-Fi");
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        movieService.deleteMovie(1L);

        verify(movieRepository).delete(movie);
    }

    @Test
    void deleteMovieThrowsWhenMovieDoesNotExist() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.deleteMovie(99L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessage("Movie not found: 99");
    }

    @Test
    void getMoviesByIdsReturnsMappedResponses() {
        when(movieRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(
                        movie(1L, "Inception", "Sci-Fi"),
                        movie(2L, "The Dark Knight", "Action")
                ));

        List<MovieResponse> responses = movieService.getMoviesByIds(List.of(1L, 2L));

        assertThat(responses)
                .extracting(MovieResponse::title)
                .containsExactly("Inception", "The Dark Knight");
    }

    private static Movie movie(Long id, String title, String genre) {
        Movie movie = new Movie();
        movie.setId(id);
        movie.setTitle(title);
        movie.setDescription("Dream heist");
        movie.setGenre(genre);
        movie.setReleaseYear(2010);
        movie.setDurationMinutes(148);
        movie.setThumbnailUrl("https://cdn.example.com/" + title.toLowerCase().replace(" ", "-") + ".jpg");
        movie.setMediaUrl("https://media.example.com/" + title.toLowerCase().replace(" ", "-") + ".mp4");
        return movie;
    }
}
