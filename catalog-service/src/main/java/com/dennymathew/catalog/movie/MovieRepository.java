package com.dennymathew.catalog.movie;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {
    boolean existsByTitleIgnoreCase(String title);

    @Query("select distinct m.genre from Movie m where m.genre is not null order by m.genre")
    List<String> findDistinctGenres();
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
