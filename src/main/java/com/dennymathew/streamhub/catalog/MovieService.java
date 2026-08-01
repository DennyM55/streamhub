package com.dennymathew.streamhub.catalog;

import com.dennymathew.streamhub.catalog.dto.CreateMovieRequest;
import com.dennymathew.streamhub.catalog.dto.MovieResponse;
import com.dennymathew.streamhub.catalog.dto.UpdateMovieRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }


    public Movie createMovie(CreateMovieRequest request) {

        Movie movie = new Movie();
        movie.setTitle(request.title());
        movie.setDescription(request.description());
        movie.setGenre(request.genre());
        movie.setReleaseYear(request.releaseYear());
        movie.setDurationMinutes(request.durationMinutes());
        movie.setThumbnailUrl(request.thumbnailUrl());
        movie.setMediaUrl(request.mediaUrl());

        return movieRepository.save(movie);
    }


    private MovieResponse toResponse(Movie movie) {
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

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));

        return toResponse(movie);
    }

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

        return toResponse(movieRepository.save(movie));
    }

    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));

        movieRepository.delete(movie);
    }

    public Page<MovieResponse> getMovies(
            String search,
            String genre,
            Integer releaseYear,
            Integer durationMinutes,
            Pageable pageable) {

        Page<Movie> movies;

        Specification<Movie> specification = Specification.allOf();

        if (search != null && !search.isBlank()) {
            specification = specification.and(
                    MovieSpecification.titleContains(search)
            );
        }

        if (genre != null && !genre.isBlank()) {
            specification = specification.and(
                    MovieSpecification.genreEquals(genre)
            );
        }
        if (releaseYear != null) {
            specification = specification.and(
                    MovieSpecification.releaseYearEquals(releaseYear)
            );
        }
        if (durationMinutes != null) {
            specification = specification.and(
                    MovieSpecification.durationLessThanOrEqual(durationMinutes)
            );
        }

        return movieRepository.findAll(specification, pageable)
                .map(this::toResponse);

    }

}