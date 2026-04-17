package com.recontent.backend.user.service;

import com.recontent.backend.audit.service.AuditLogService;
import com.recontent.backend.exception.ApiException;
import com.recontent.backend.security.CurrentUserService;
import com.recontent.backend.user.dto.UpdateUserRequest;
import com.recontent.backend.user.dto.UserResponse;
import com.recontent.backend.user.entity.UserEntity;
import com.recontent.backend.user.mapper.UserMapper;
import com.recontent.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository, UserMapper userMapper, CurrentUserService currentUserService, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return userMapper.toResponse(currentUser());
    }

    @Transactional
    public UserResponse updateMe(UpdateUserRequest request) {
        UserEntity user = currentUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setAvatarUrl(request.avatarUrl());
        auditLogService.record(user.getId(), "USER_PROFILE_UPDATED", "USER", user.getId().toString(), Map.of());
        return userMapper.toResponse(userRepository.save(user));
    }

    private UserEntity currentUser() {
        UUID userId = currentUserService.requireCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Current user was not found"));
    }
}
