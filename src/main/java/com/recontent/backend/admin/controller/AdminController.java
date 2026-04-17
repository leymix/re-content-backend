package com.recontent.backend.admin.controller;

import com.recontent.backend.admin.dto.AdminUserResponse;
import com.recontent.backend.admin.dto.HealthSummaryResponse;
import com.recontent.backend.admin.service.AdminService;
import com.recontent.backend.audit.dto.AuditLogResponse;
import com.recontent.backend.audit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final AuditLogService auditLogService;

    public AdminController(AdminService adminService, AuditLogService auditLogService) {
        this.adminService = adminService;
        this.auditLogService = auditLogService;
    }

    @Operation(summary = "List users")
    @GetMapping("/users")
    public List<AdminUserResponse> users() {
        return adminService.users();
    }

    @Operation(summary = "List audit logs")
    @GetMapping("/audit-logs")
    public Page<AuditLogResponse> auditLogs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return auditLogService.list(page, Math.min(size, 100));
    }

    @Operation(summary = "Get admin health summary")
    @GetMapping("/health-summary")
    public HealthSummaryResponse healthSummary() {
        return adminService.healthSummary();
    }
}
