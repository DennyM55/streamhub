package com.dennymathew.catalog.common;

public record ApiError(
        int status,
        String message
) {
}
