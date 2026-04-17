package com.recontent.backend.auth.controller;

import com.recontent.backend.auth.dto.AuthResponse;
import com.recontent.backend.auth.dto.AuthResult;
import com.recontent.backend.auth.dto.LoginRequest;
import com.recontent.backend.auth.dto.RegisterRequest;
import com.recontent.backend.auth.service.AuthService;
import com.recontent.backend.config.AppProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final AppProperties properties;

    public AuthController(AuthService authService, AppProperties properties) {
        this.authService = authService;
        this.properties = properties;
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse servletResponse) {
        AuthResult result = authService.register(request);
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken(), result.refreshTokenMaxAgeSeconds()).toString());
        return ResponseEntity.status(201).body(result.response());
    }

    @Operation(summary = "Login with username/email and password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse servletResponse) {
        AuthResult result = authService.login(request);
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken(), result.refreshTokenMaxAgeSeconds()).toString());
        return ResponseEntity.ok(result.response());
    }

    @Operation(summary = "Rotate refresh token and issue a new access token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "refresh_token") String refreshToken, HttpServletResponse servletResponse) {
        AuthResult result = authService.refresh(refreshToken);
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken(), result.refreshTokenMaxAgeSeconds()).toString());
        return ResponseEntity.ok(result.response());
    }

    @Operation(summary = "Logout and revoke the current refresh token")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token", required = false) String refreshToken, HttpServletResponse servletResponse) {
        authService.logout(refreshToken);
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get the current authenticated user")
    @GetMapping("/me")
    public AuthResponse me() {
        return authService.currentUser();
    }

    private ResponseCookie refreshCookie(String token, long maxAgeSeconds) {
        AppProperties.RefreshCookie cookie = properties.security().refreshCookie();
        return ResponseCookie.from(cookie.name(), token)
                .httpOnly(true)
                .secure(cookie.secure())
                .sameSite(cookie.sameSite())
                .path(cookie.path())
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        AppProperties.RefreshCookie cookie = properties.security().refreshCookie();
        return ResponseCookie.from(cookie.name(), "")
                .httpOnly(true)
                .secure(cookie.secure())
                .sameSite(cookie.sameSite())
                .path(cookie.path())
                .maxAge(0)
                .build();
    }
}
