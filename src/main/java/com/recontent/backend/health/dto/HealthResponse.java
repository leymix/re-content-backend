package com.recontent.backend.health.dto;

import java.time.Instant;

public record HealthResponse(
        String status,
        String service,
        String version,
        Instant timestamp
) {
}
