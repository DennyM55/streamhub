package com.dennymathew.catalog.movie;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Locale;

final class MovieSpecification {
    private MovieSpecification() { }

    static Specification<Movie> matching(String search, String genre, Integer releaseYear, Integer durationMinutes) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (StringUtils.hasText(search)) {
                String escaped = search.trim().toLowerCase(Locale.ROOT)
                        .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + escaped + "%", '\\'));
            }
            if (StringUtils.hasText(genre)) {
                predicates.add(cb.equal(cb.lower(root.get("genre")), genre.trim().toLowerCase(Locale.ROOT)));
            }
            if (releaseYear != null) {
                predicates.add(cb.equal(root.get("releaseYear"), releaseYear));
            }
            if (durationMinutes != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("durationMinutes"), durationMinutes));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
