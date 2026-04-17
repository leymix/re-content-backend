package com.recontent.backend.admin.dto;

import com.recontent.backend.common.enums.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String username,
        String email,
        UserStatus status,
        Set<String> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
