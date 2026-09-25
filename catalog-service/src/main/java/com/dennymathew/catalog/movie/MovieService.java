package com.dennymathew.catalog.movie;

import com.dennymathew.catalog.movie.dto.CreateMovieRequest;
import com.dennymathew.catalog.movie.dto.MovieResponse;
import com.dennymathew.catalog.movie.dto.UpdateMovieRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Cacheable(cacheNames = "movies", key = "#id")
    public MovieResponse getMovie(Long id) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));

        return toMovieResponse(movie);
    }

    public MovieResponse createMovie(CreateMovieRequest request) {

        Movie movie = new Movie();
        movie.setTitle(request.title());
        movie.setDescription(request.description());
        movie.setGenre(request.genre());
        movie.setReleaseYear(request.releaseYear());
        movie.setDurationMinutes(request.durationMinutes());
        movie.setThumbnailUrl(request.thumbnailUrl());
        movie.setMediaUrl(request.mediaUrl());

        Movie savedMovie = movieRepository.save(movie);

        return toMovieResponse(savedMovie);
    }

    public Page<MovieResponse> getMovies(String search, String genre, Pageable pageable) {

        boolean hasSearch = StringUtils.hasText(search);
        boolean hasGenre = StringUtils.hasText(genre);

        Page<Movie> movies;

        if (hasSearch && hasGenre) {
            movies = movieRepository.findByTitleContainingIgnoreCaseAndGenreIgnoreCase(search, genre, pageable);
        } else if (hasSearch) {
            movies = movieRepository.findByTitleContainingIgnoreCase(search, pageable);
        } else if (hasGenre) {
            movies = movieRepository.findByGenreIgnoreCase(genre, pageable);
        } else {
            movies = movieRepository.findAll(pageable);
        }

        return movies.map(this::toMovieResponse);
    }

    public Page<MovieResponse> getMovies(String search, String genre, Integer releaseYear,
                                        Integer durationMinutes, Pageable pageable) {
        if ((releaseYear != null && releaseYear < 1) || (durationMinutes != null && durationMinutes < 1)) {
            throw new IllegalArgumentException("releaseYear and durationMinutes must be positive");
        }
        return movieRepository.findAll(MovieSpecification.matching(search, genre, releaseYear, durationMinutes), pageable)
                .map(this::toMovieResponse);
    }

    public List<String> getGenres() {
        return movieRepository.findDistinctGenres();
    }

    @Transactional
    @CacheEvict(cacheNames = "movies", key = "#id")
    public MovieResponse updateMovie(Long id, UpdateMovieRequest request) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));

        movie.setTitle(request.title());
        movie.setDescription(request.description());
        movie.setGenre(request.genre());
        movie.setReleaseYear(request.releaseYear());
        movie.setDurationMinutes(request.durationMinutes());
        movie.setThumbnailUrl(request.thumbnailUrl());
        movie.setMediaUrl(request.mediaUrl());

        return toMovieResponse(movie);
    }

    @CacheEvict(cacheNames = "movies", key = "#id")
    public void deleteMovie(Long id) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));

        movieRepository.delete(movie);
    }

    private MovieResponse toMovieResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getGenre(),
                movie.getReleaseYear(),
                movie.getDurationMinutes(),
                movie.getThumbnailUrl(),
                movie.getMediaUrl()
        );
    }

    public List<MovieResponse> getMoviesByIds(List<Long> ids) {
        if (ids.isEmpty() || ids.size() > 50 || ids.stream().anyMatch(id -> id == null || id < 1)) {
            throw new IllegalArgumentException("Provide between 1 and 50 positive movie IDs");
        }
        return movieRepository.findAllById(ids)
                .stream()
                .map(this::toMovieResponse)
                .toList();
    }
}
