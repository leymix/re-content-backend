package com.recontent.backend.watchlist.dto;

import com.recontent.backend.common.enums.MediaType;
import com.recontent.backend.common.enums.WatchlistStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record WatchlistResponse(
        UUID id,
        MediaType mediaType,
        Long mediaId,
        String title,
        String posterPath,
        String backdropPath,
        String overview,
        LocalDate releaseDate,
        BigDecimal voteAverage,
        WatchlistStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
