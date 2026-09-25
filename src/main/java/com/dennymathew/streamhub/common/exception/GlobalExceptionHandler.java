package com.dennymathew.streamhub.common.exception;

import com.dennymathew.streamhub.catalog.MovieNotFoundException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;

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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        return ResponseEntity.badRequest()
                .body(new ApiError(400, "A record with these details already exists"));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiError> handleServiceUnavailable(
            ResourceAccessException ex) {

        return ResponseEntity.status(503)
                .body(new ApiError(503, "Catalog service unavailable"));
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ApiError> handleCircuitOpen(
            CallNotPermittedException ex) {

        return ResponseEntity.status(503)
                .body(new ApiError(503, "Catalog service temporarily unavailable"));
    }
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiError> handleCatalogClientError(HttpClientErrorException ex) {
        int status = ex.getStatusCode().value();
        String message = status == 404 ? "Movie not found" : "Catalog request rejected";
        return ResponseEntity.status(status).body(new ApiError(status, message));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ApiError> handleCatalogServerError(HttpServerErrorException ex) {
        return ResponseEntity.status(503).body(new ApiError(503, "Catalog service unavailable"));
    }

    @ExceptionHandler({HandlerMethodValidationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> handleMalformedRequest(Exception ex) {
        return ResponseEntity.badRequest().body(new ApiError(400, "Invalid request parameters"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidLogin(BadCredentialsException ex) {
        return ResponseEntity.status(401).body(new ApiError(401, "Invalid email or password"));
    }
}