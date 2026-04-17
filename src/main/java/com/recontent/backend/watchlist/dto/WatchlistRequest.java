package com.recontent.backend.watchlist.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.recontent.backend.common.enums.MediaType;
import com.recontent.backend.common.enums.WatchlistStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WatchlistRequest(
        @NotNull MediaType mediaType,
        @NotNull @Positive @JsonAlias("id") Long mediaId,
        @NotBlank @Size(max = 300) String title,
        @Size(max = 1000) @JsonAlias("poster_path") String posterPath,
        @Size(max = 1000) @JsonAlias("backdrop_path") String backdropPath,
        @Size(max = 5000) String overview,
        @JsonAlias({"release_date", "first_air_date"}) LocalDate releaseDate,
        @DecimalMin("0.0") @DecimalMax("10.0") @JsonAlias("vote_average") BigDecimal voteAverage,
        WatchlistStatus status
) {
}
