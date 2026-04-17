package com.recontent.backend.user.dto;

import com.recontent.backend.common.enums.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        String avatarUrl,
        UserStatus status,
        Set<String> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
