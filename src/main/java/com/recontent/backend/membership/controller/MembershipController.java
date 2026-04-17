package com.recontent.backend.membership.controller;

import com.recontent.backend.membership.dto.MembershipPlanResponse;
import com.recontent.backend.membership.dto.SubscribeRequest;
import com.recontent.backend.membership.dto.UserMembershipResponse;
import com.recontent.backend.membership.service.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Membership")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1")
public class MembershipController {
    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @Operation(summary = "List active membership plans")
    @GetMapping("/membership/plans")
    public List<MembershipPlanResponse> plans() {
        return membershipService.activePlans();
    }

    @Operation(summary = "Subscribe current user to a plan")
    @PostMapping("/membership/subscribe")
    public ResponseEntity<UserMembershipResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        return ResponseEntity.status(201).body(membershipService.subscribe(request));
    }

    @Operation(summary = "Get current user's active membership")
    @GetMapping("/users/me/membership")
    public UserMembershipResponse currentMembership() {
        return membershipService.currentMembership();
    }
}
