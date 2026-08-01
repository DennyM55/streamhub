package com.dennymathew.streamhub.catalog;

import org.springframework.data.jpa.domain.Specification;

public class MovieSpecification {

    public static Specification<Movie> titleContains(String search) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + search.toLowerCase() + "%"
                );
    }
    public static Specification<Movie> genreEquals(String genre) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("genre")),
                        genre.toLowerCase()
                );
    }
    public static Specification<Movie> releaseYearEquals(Integer releaseYear) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("releaseYear"),
                        releaseYear
                );
    }
    public static Specification<Movie> durationLessThanOrEqual(Integer durationMinutes) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("durationMinutes"),
                        durationMinutes
                );
    }
}