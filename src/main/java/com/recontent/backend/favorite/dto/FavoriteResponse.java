package com.recontent.backend.favorite.dto;

import com.recontent.backend.common.enums.MediaType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FavoriteResponse(
        UUID id,
        MediaType mediaType,
        Long mediaId,
        String title,
        String posterPath,
        String backdropPath,
        String overview,
        LocalDate releaseDate,
        BigDecimal voteAverage,
        Instant createdAt
) {
}
