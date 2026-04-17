package com.recontent.backend.auth.service;

import com.recontent.backend.audit.service.AuditLogService;
import com.recontent.backend.auth.dto.AuthResponse;
import com.recontent.backend.auth.dto.AuthResult;
import com.recontent.backend.auth.dto.LoginRequest;
import com.recontent.backend.auth.dto.RegisterRequest;
import com.recontent.backend.auth.entity.RefreshTokenEntity;
import com.recontent.backend.common.enums.RoleName;
import com.recontent.backend.common.enums.UserStatus;
import com.recontent.backend.exception.ApiException;
import com.recontent.backend.security.CurrentUserService;
import com.recontent.backend.security.JwtService;
import com.recontent.backend.user.entity.RoleEntity;
import com.recontent.backend.user.entity.UserEntity;
import com.recontent.backend.user.mapper.UserMapper;
import com.recontent.backend.user.repository.RoleRepository;
import com.recontent.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, RefreshTokenService refreshTokenService, UserMapper userMapper,
                       CurrentUserService currentUserService, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AuthResult register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email is already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "USERNAME_ALREADY_EXISTS", "Username is already taken");
        }

        RoleEntity userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "ROLE_NOT_SEEDED", "Default USER role is missing"));

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.getRoles().add(userRole);
        UserEntity saved = userRepository.save(user);
        auditLogService.record(saved.getId(), "AUTH_REGISTER", "USER", saved.getId().toString(), Map.of("email", email));
        return issueAuthResult(saved);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        UserEntity user = findByLogin(request.login());
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid login or password");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "USER_NOT_ACTIVE", "User account is not active");
        }
        auditLogService.record(user.getId(), "AUTH_LOGIN", "USER", user.getId().toString(), Map.of());
        return issueAuthResult(user);
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        RefreshTokenEntity oldToken = refreshTokenService.consumeForRotation(rawRefreshToken);
        AuthResult result = issueAuthResult(oldToken.getUser());
        refreshTokenService.attachReplacement(oldToken, result.refreshToken());
        auditLogService.record(oldToken.getUser().getId(), "AUTH_REFRESH", "USER", oldToken.getUser().getId().toString(), Map.of());
        return result;
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
        UUID userId = null;
        try {
            userId = currentUserService.requireCurrentUserId();
        } catch (ApiException ignored) {
            // Logout still clears the cookie if the access token is already expired.
        }
        auditLogService.record(userId, "AUTH_LOGOUT", "USER", userId == null ? null : userId.toString(), Map.of());
    }

    @Transactional(readOnly = true)
    public AuthResponse currentUser() {
        UUID userId = currentUserService.requireCurrentUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Current user was not found"));
        return new AuthResponse(null, "Bearer", 0, userMapper.toResponse(user));
    }

    private AuthResult issueAuthResult(UserEntity user) {
        String accessToken = jwtService.createAccessToken(user);
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(user);
        AuthResponse response = new AuthResponse(accessToken, "Bearer", jwtService.accessTokenTtlSeconds(), userMapper.toResponse(user));
        return new AuthResult(response, refreshToken.rawToken(), refreshToken.maxAgeSeconds());
    }

    private UserEntity findByLogin(String login) {
        String normalized = login.trim();
        return userRepository.findByEmailIgnoreCase(normalized)
                .or(() -> userRepository.findByUsernameIgnoreCase(normalized))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid login or password"));
    }
}
