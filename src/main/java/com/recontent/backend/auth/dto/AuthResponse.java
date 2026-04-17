package com.recontent.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.recontent.backend.user.dto.UserResponse;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserResponse user
) {
}
