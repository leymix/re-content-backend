package com.recontent.backend.rating.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RatingUpdateRequest(@NotNull @DecimalMin("0.5") @DecimalMax("10.0") BigDecimal score) {
}
