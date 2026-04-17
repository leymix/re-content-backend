package com.recontent.backend.membership.dto;

import com.recontent.backend.common.enums.DurationType;

import java.math.BigDecimal;
import java.util.UUID;

public record MembershipPlanResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        String currency,
        DurationType durationType,
        boolean active
) {
}
