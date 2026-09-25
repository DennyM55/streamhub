package com.dennymathew.catalog.movie;

import com.dennymathew.catalog.common.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MovieControllerTest {
    private MovieService service;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        service = mock(MovieService.class);
        mvc = MockMvcBuilders.standaloneSetup(new MovieController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void rejectsInvalidPageSizeAndSortBeforeCallingRepository() throws Exception {
        mvc.perform(get("/movies").param("page", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/movies").param("size", "5000")).andExpect(status().isBadRequest());
        mvc.perform(get("/movies").param("size", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/movies").param("sort", "description,asc")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void forwardsAllFiltersAndRepeatedSortParameters() throws Exception {
        when(service.getMovies(eq("dragon"), eq("Fantasy"), eq(2010), eq(15), any(Pageable.class)))
                .thenReturn(Page.empty(org.springframework.data.domain.PageRequest.of(0, 20)));
        mvc.perform(get("/movies").param("search", "dragon").param("genre", "Fantasy")
                        .param("releaseYear", "2010").param("durationMinutes", "15")
                        .param("sort", "releaseYear,desc", "title,asc"))
                .andExpect(status().isOk());
        verify(service).getMovies(eq("dragon"), eq("Fantasy"), eq(2010), eq(15), argThat(pageable ->
                pageable.getPageSize() == 20 && pageable.getSort().getOrderFor("releaseYear").isDescending()
                        && pageable.getSort().getOrderFor("title").isAscending()));
    }

    @Test
    void genresHasItsOwnRouteAndDoesNotParseAsAMovieId() throws Exception {
        when(service.getGenres()).thenReturn(java.util.List.of("Action", "Fantasy"));
        mvc.perform(get("/movies/genres")).andExpect(status().isOk());
        verify(service).getGenres();
    }
}
