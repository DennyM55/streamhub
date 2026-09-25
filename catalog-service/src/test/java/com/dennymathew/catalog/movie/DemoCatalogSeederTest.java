package com.dennymathew.catalog.movie;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DemoCatalogSeederTest {
    @Test
    void seedsRealTitlesWithCreditsAndOnlyMatchingVideoAssets() {
        MovieRepository repository = mock(MovieRepository.class);
        new DemoCatalogSeeder(repository).run(new DefaultApplicationArguments());
        var films = ArgumentCaptor.forClass(Movie.class);
        verify(repository, times(12)).save(films.capture());
        assertThat(films.getAllValues()).extracting(Movie::getTitle).doesNotHaveDuplicates();
        assertThat(films.getAllValues()).allSatisfy(movie -> {
            assertThat(movie.getDescription()).contains("Blender", "https://studio.blender.org/films/").hasSizeLessThanOrEqualTo(255);
            assertThat(movie.getDurationMinutes()).isPositive();
        });
        assertThat(films.getAllValues().stream().filter(movie -> movie.getMediaUrl() != null)).hasSize(4);
    }

    @Test
    void leavesExistingMoviesUntouchedOnRestart() {
        MovieRepository repository = mock(MovieRepository.class);
        when(repository.existsByTitleIgnoreCase(anyString())).thenReturn(true);
        new DemoCatalogSeeder(repository).run(new DefaultApplicationArguments());
        verify(repository, never()).save(any());
    }
}
