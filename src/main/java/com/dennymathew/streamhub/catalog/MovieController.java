package com.dennymathew.streamhub.catalog;

import com.dennymathew.streamhub.catalog.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {
    private final CatalogClient catalogClient;
    public MovieController(CatalogClient catalogClient) { this.catalogClient = catalogClient; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse createMovie(@Valid @RequestBody CreateMovieRequest movie) {
        return catalogClient.createMovie(movie);
    }
    @GetMapping("/{id}")
    public MovieResponse getMovieById(@PathVariable Long id) { return catalogClient.getMovie(id); }
    @PutMapping("/{id}")
    public MovieResponse updateMovie(@PathVariable Long id, @Valid @RequestBody UpdateMovieRequest request) {
        return catalogClient.updateMovie(id, request);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovie(@PathVariable Long id) { catalogClient.deleteMovie(id); }
    @GetMapping
    public MoviePage getMovies(@RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer releaseYear,
            @RequestParam(required = false) Integer durationMinutes,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        return catalogClient.getMovies(search, genre, releaseYear, durationMinutes, page, size, sort);
    }
    @GetMapping("/genres")
    public List<String> getGenres() { return catalogClient.getGenres(); }
    @GetMapping("/remote/{id}")
    public MovieResponse getRemoteMovie(@PathVariable Long id) { return catalogClient.getMovie(id); }
}
