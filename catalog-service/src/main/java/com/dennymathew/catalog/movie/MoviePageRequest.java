package com.dennymathew.catalog.movie;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class MoviePageRequest {
    private static final Set<String> SORT_FIELDS = Set.of("id", "title", "genre", "releaseYear", "durationMinutes");

    private MoviePageRequest() { }

    static PageRequest of(int page, int size, List<String> sortValues) {
        if (page < 0 || size < 1 || size > 50) {
            throw new IllegalArgumentException("page must be >= 0 and size must be between 1 and 50");
        }
        List<Sort.Order> orders = new ArrayList<>();
        if (sortValues != null) {
            for (String value : sortValues) {
                String[] parts = value.split(",", -1);
                if (parts.length > 2 || !SORT_FIELDS.contains(parts[0])) {
                    throw new IllegalArgumentException("Unsupported movie sort field");
                }
                String direction = parts.length == 1 ? "asc" : parts[1].toLowerCase(Locale.ROOT);
                if (!direction.equals("asc") && !direction.equals("desc")) {
                    throw new IllegalArgumentException("Sort direction must be asc or desc");
                }
                orders.add(new Sort.Order(Sort.Direction.fromString(direction), parts[0]));
            }
        }
        // The ID tie-breaker keeps pagination stable when titles or years are equal.
        if (orders.stream().noneMatch(order -> order.getProperty().equals("id"))) {
            orders.add(Sort.Order.asc("id"));
        }
        return PageRequest.of(page, size, Sort.by(orders));
    }
}
