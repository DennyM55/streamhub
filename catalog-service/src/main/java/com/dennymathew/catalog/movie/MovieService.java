package com.dennymathew.catalog.movie;

import com.dennymathew.catalog.movie.dto.CreateMovieRequest;
import com.dennymathew.catalog.movie.dto.MovieResponse;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

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
}
