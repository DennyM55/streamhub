package com.dennymathew.catalog.movie;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoviePageRequestTest {
    @Test
    void rejectsUnboundedOrInvalidPages() {
        for (int[] values : new int[][]{{-1, 20}, {0, 0}, {0, -1}, {0, 51}}) {
            assertThatThrownBy(() -> MoviePageRequest.of(values[0], values[1], null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void rejectsUnknownPropertiesAndMalformedDirections() {
        for (String sort : List.of("password,asc", "title,random", "title,asc,desc", "", "title,")) {
            assertThatThrownBy(() -> MoviePageRequest.of(0, 20, List.of(sort)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void supportsMultipleSortsAndStableIdTieBreaker() {
        var request = MoviePageRequest.of(2, 10, List.of("releaseYear,desc", "title,asc"));
        assertThat(request.getPageNumber()).isEqualTo(2);
        assertThat(request.getPageSize()).isEqualTo(10);
        assertThat(request.getSort().stream().map(order -> order.getProperty()).toList())
                .containsExactly("releaseYear", "title", "id");
        assertThat(request.getSort().getOrderFor("releaseYear").isDescending()).isTrue();
    }
}
