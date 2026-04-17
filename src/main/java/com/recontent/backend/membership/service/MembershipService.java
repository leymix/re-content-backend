package com.recontent.backend.membership.service;

import com.recontent.backend.audit.service.AuditLogService;
import com.recontent.backend.common.enums.DurationType;
import com.recontent.backend.common.enums.MembershipStatus;
import com.recontent.backend.exception.ApiException;
import com.recontent.backend.membership.dto.MembershipPlanResponse;
import com.recontent.backend.membership.dto.SubscribeRequest;
import com.recontent.backend.membership.dto.UserMembershipResponse;
import com.recontent.backend.membership.entity.MembershipPlanEntity;
import com.recontent.backend.membership.entity.UserMembershipEntity;
import com.recontent.backend.membership.mapper.MembershipMapper;
import com.recontent.backend.membership.repository.MembershipPlanRepository;
import com.recontent.backend.membership.repository.UserMembershipRepository;
import com.recontent.backend.security.CurrentUserService;
import com.recontent.backend.user.entity.UserEntity;
import com.recontent.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MembershipService {
    private final MembershipPlanRepository membershipPlanRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final MembershipMapper membershipMapper;
    private final AuditLogService auditLogService;

    public MembershipService(MembershipPlanRepository membershipPlanRepository, UserMembershipRepository userMembershipRepository,
                             UserRepository userRepository, CurrentUserService currentUserService,
                             MembershipMapper membershipMapper, AuditLogService auditLogService) {
        this.membershipPlanRepository = membershipPlanRepository;
        this.userMembershipRepository = userMembershipRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.membershipMapper = membershipMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> activePlans() {
        return membershipPlanRepository.findAllByActiveTrueOrderByPriceAsc().stream()
                .map(membershipMapper::toPlanResponse)
                .toList();
    }

    @Transactional
    public UserMembershipResponse subscribe(SubscribeRequest request) {
        UUID userId = currentUserService.requireCurrentUserId();
        MembershipPlanEntity plan = membershipPlanRepository.findById(request.planId())
                .filter(MembershipPlanEntity::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEMBERSHIP_PLAN_NOT_FOUND", "Active membership plan was not found"));

        userMembershipRepository.findFirstByUser_IdAndStatusOrderByStartDateDesc(userId, MembershipStatus.ACTIVE)
                .ifPresent(existing -> {
                    existing.setStatus(MembershipStatus.CANCELED);
                    existing.setEndDate(Instant.now());
                    userMembershipRepository.save(existing);
                });

        Instant start = Instant.now();
        UserMembershipEntity membership = new UserMembershipEntity();
        membership.setUser(currentUser(userId));
        membership.setPlan(plan);
        membership.setStartDate(start);
        membership.setEndDate(resolveEndDate(start, plan.getDurationType()));
        membership.setStatus(MembershipStatus.ACTIVE);
        UserMembershipEntity saved = userMembershipRepository.save(membership);
        auditLogService.record(userId, "MEMBERSHIP_SUBSCRIBED", "USER_MEMBERSHIP", saved.getId().toString(), Map.of("planId", plan.getId()));
        return membershipMapper.toMembershipResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserMembershipResponse currentMembership() {
        UUID userId = currentUserService.requireCurrentUserId();
        return userMembershipRepository.findFirstByUser_IdAndStatusOrderByStartDateDesc(userId, MembershipStatus.ACTIVE)
                .map(membershipMapper::toMembershipResponse)
                .orElse(null);
    }

    private Instant resolveEndDate(Instant start, DurationType durationType) {
        return switch (durationType) {
            case FREE -> null;
            case MONTHLY -> start.plus(30, ChronoUnit.DAYS);
            case YEARLY -> start.plus(365, ChronoUnit.DAYS);
        };
    }

    private UserEntity currentUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Current user was not found"));
    }
}
