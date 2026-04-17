package com.recontent.backend.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID userId,
        String action,
        String entityType,
        String entityId,
        String metadataJson,
        Instant createdAt
) {
}
