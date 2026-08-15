package com.dennymathew.catalog.movie;

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
