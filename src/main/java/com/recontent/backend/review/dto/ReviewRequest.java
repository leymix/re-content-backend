package com.recontent.backend.review.dto;

import com.recontent.backend.common.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull MediaType mediaType,
        @NotNull @Positive Long mediaId,
        @NotBlank @Size(min = 2, max = 5000) String content,
        boolean spoilerFlag
) {
}
