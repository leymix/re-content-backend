package com.recontent.backend.membership.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.recontent.backend.common.enums.MembershipStatus;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserMembershipResponse(
        UUID id,
        MembershipPlanResponse plan,
        Instant startDate,
        Instant endDate,
        MembershipStatus status
) {
}
