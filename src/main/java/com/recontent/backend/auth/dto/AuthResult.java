package com.recontent.backend.auth.dto;

public record AuthResult(
        AuthResponse response,
        String refreshToken,
        long refreshTokenMaxAgeSeconds
) {
}
