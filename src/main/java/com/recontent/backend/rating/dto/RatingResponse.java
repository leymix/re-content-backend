package com.recontent.backend.rating.dto;

import com.recontent.backend.common.enums.MediaType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RatingResponse(
        UUID id,
        MediaType mediaType,
        Long mediaId,
        BigDecimal score,
        Instant createdAt,
        Instant updatedAt
) {
}
