package com.dennymathew.catalog.movie;

public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(Long id) {
        super("Movie not found: " + id);
    }
}
