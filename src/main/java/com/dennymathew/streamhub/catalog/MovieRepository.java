package com.dennymathew.streamhub.catalog;

import com.dennymathew.streamhub.catalog.dto.MovieResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {
    Page<Movie> findByTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );

    Page<Movie> findByGenreIgnoreCase(
            String genre,
            Pageable pageable
    );
    Page<Movie> findByTitleContainingIgnoreCaseAndGenreIgnoreCase(
            String title,
            String genre,
            Pageable pageable
    );
}
