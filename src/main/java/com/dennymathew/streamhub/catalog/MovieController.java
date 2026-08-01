package com.dennymathew.streamhub.catalog;

import com.dennymathew.streamhub.catalog.dto.CreateMovieRequest;
import com.dennymathew.streamhub.catalog.dto.MovieResponse;
import com.dennymathew.streamhub.catalog.dto.UpdateMovieRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public Movie createMovie(@Valid @RequestBody CreateMovieRequest movie) {
        return movieService.createMovie(movie);
    }

    @GetMapping("/{id}")
    public MovieResponse getMovieById(@PathVariable Long id) {
        return movieService.getMovieById(id);
    }

    @PutMapping("/{id}")
    public MovieResponse updateMovie(
            @PathVariable Long id,
            @RequestBody UpdateMovieRequest request) {

        return movieService.updateMovie(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
    }
    @GetMapping
    public Page<MovieResponse> getMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer releaseYear,
            @RequestParam(required = false) Integer durationMinutes,
            Pageable pageable) {

        return movieService.getMovies(search, genre, releaseYear, durationMinutes, pageable);
    }

}