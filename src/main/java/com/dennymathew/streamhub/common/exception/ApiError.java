package com.dennymathew.streamhub.common.exception;

public record ApiError(
        int status,
        String message
) {}