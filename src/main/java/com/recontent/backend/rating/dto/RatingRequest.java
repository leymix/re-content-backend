package com.recontent.backend.rating.dto;

import com.recontent.backend.common.enums.MediaType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RatingRequest(
        @NotNull MediaType mediaType,
        @NotNull @Positive Long mediaId,
        @NotNull @DecimalMin("0.5") @DecimalMax("10.0") BigDecimal score
) {
}
