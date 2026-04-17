package com.recontent.backend.review.dto;

import com.recontent.backend.common.enums.MediaType;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        MediaType mediaType,
        Long mediaId,
        String content,
        boolean spoilerFlag,
        Author author,
        Instant createdAt,
        Instant updatedAt
) {
    public record Author(UUID id, String username, String avatarUrl) {
    }
}
