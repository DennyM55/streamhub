package com.dennymathew.streamhub.user.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(
        @NotBlank(message = "Name is required")
        String name
) {
}