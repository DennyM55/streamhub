package com.dennymathew.catalog.movie;

import com.dennymathew.catalog.movie.dto.CreateMovieRequest;
import com.dennymathew.catalog.movie.dto.MovieResponse;
import com.dennymathew.catalog.movie.dto.UpdateMovieRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/{id}")
    public MovieResponse getMovie(@PathVariable Long id) {
        return movieService.getMovie(id);
    }

    @GetMapping
    public Page<MovieResponse> getMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            Pageable pageable
    ) {
        return movieService.getMovies(search, genre, pageable);
    }

    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody CreateMovieRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(movieService.createMovie(request));
    }

    @PutMapping("/{id}")
    public MovieResponse updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMovieRequest request
    ) {
        return movieService.updateMovie(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/batch")
    public List<MovieResponse> getMoviesByIds(@RequestParam List<Long> ids) {
        return movieService.getMoviesByIds(ids);
    }
}
