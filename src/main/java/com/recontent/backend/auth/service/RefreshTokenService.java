package com.recontent.backend.auth.service;

import com.recontent.backend.auth.entity.RefreshTokenEntity;
import com.recontent.backend.auth.repository.RefreshTokenRepository;
import com.recontent.backend.config.AppProperties;
import com.recontent.backend.exception.ApiException;
import com.recontent.backend.user.entity.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class RefreshTokenService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties properties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, AppProperties properties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.properties = properties;
    }

    @Transactional
    public IssuedRefreshToken issue(UserEntity user) {
        String rawToken = randomToken();
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUser(user);
        entity.setTokenHash(hash(rawToken));
        entity.setExpiresAt(Instant.now().plus(properties.security().refreshCookie().ttlDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(entity);
        return new IssuedRefreshToken(rawToken, properties.security().refreshCookie().ttlDays() * 24 * 60 * 60);
    }

    @Transactional
    public RefreshTokenEntity consumeForRotation(String rawToken) {
        String oldHash = hash(rawToken);
        RefreshTokenEntity token = refreshTokenRepository.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(oldHash, Instant.now())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired"));
        token.setRevokedAt(Instant.now());
        return token;
    }

    @Transactional
    public void attachReplacement(RefreshTokenEntity oldToken, String newRawToken) {
        oldToken.setReplacedByTokenHash(hash(newRawToken));
        refreshTokenRepository.save(oldToken);
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(hash(rawToken), Instant.now())
                .ifPresent(token -> {
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String randomToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record IssuedRefreshToken(String rawToken, long maxAgeSeconds) {
    }
}
