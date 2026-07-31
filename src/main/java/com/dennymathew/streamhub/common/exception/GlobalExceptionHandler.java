package com.dennymathew.streamhub.common.exception;

import com.dennymathew.streamhub.catalog.MovieNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return ResponseEntity.badRequest()
                .body(new ApiError(400, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity.badRequest()
                .body(new ApiError(400, ex.getMessage()));
    }

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<ApiError> handleMovieNotFound(MovieNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(new ApiError(404, ex.getMessage()));
    }
}