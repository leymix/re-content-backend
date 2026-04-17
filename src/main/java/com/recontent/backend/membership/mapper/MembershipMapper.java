package com.recontent.backend.membership.mapper;

import com.recontent.backend.membership.dto.MembershipPlanResponse;
import com.recontent.backend.membership.dto.UserMembershipResponse;
import com.recontent.backend.membership.entity.MembershipPlanEntity;
import com.recontent.backend.membership.entity.UserMembershipEntity;
import org.springframework.stereotype.Component;

@Component
public class MembershipMapper {
    public MembershipPlanResponse toPlanResponse(MembershipPlanEntity plan) {
        return new MembershipPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getDescription(),
                plan.getPrice(),
                plan.getCurrency(),
                plan.getDurationType(),
                plan.isActive()
        );
    }

    public UserMembershipResponse toMembershipResponse(UserMembershipEntity membership) {
        return new UserMembershipResponse(
                membership.getId(),
                toPlanResponse(membership.getPlan()),
                membership.getStartDate(),
                membership.getEndDate(),
                membership.getStatus()
        );
    }
}
