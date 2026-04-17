package com.recontent.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(max = 255) String login,
        @NotBlank @Size(max = 120) String password
) {
}
