package com.recontent.backend.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid Security security,
        @Valid Cors cors
) {
    public record Security(@Valid Jwt jwt, @Valid RefreshCookie refreshCookie) {
    }

    public record Jwt(@NotBlank String issuer, @NotBlank String secret, @Min(1) long accessTokenTtlMinutes) {
    }

    public record RefreshCookie(
            @NotBlank String name,
            boolean secure,
            @NotBlank String sameSite,
            @NotBlank String path,
            @Min(1) long ttlDays
    ) {
    }

    public record Cors(@NotEmpty List<String> allowedOrigins) {
    }
}
