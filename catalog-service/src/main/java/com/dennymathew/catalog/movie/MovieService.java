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
        return movieRepository.findAllById(ids)
                .stream()
                .map(this::toMovieResponse)
                .toList();
    }
}
