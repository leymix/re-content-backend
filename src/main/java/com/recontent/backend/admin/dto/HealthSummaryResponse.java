package com.recontent.backend.admin.dto;

public record HealthSummaryResponse(
        String status,
        long users,
        long favorites,
        long watchlistItems,
        long ratings,
        long reviews,
        long auditLogs
) {
}
