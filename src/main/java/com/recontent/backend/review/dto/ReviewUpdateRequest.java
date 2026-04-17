package com.recontent.backend.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewUpdateRequest(
        @NotBlank @Size(min = 2, max = 5000) String content,
        boolean spoilerFlag
) {
}
