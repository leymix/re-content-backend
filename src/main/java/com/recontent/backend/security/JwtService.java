package com.recontent.backend.security;

import com.recontent.backend.config.AppProperties;
import com.recontent.backend.user.entity.RoleEntity;
import com.recontent.backend.user.entity.UserEntity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final AppProperties properties;

    public JwtService(JwtEncoder jwtEncoder, AppProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public String createAccessToken(UserEntity user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.security().jwt().accessTokenTtlMinutes(), ChronoUnit.MINUTES);
        List<String> roles = user.getRoles().stream()
                .map(RoleEntity::getName)
                .map(Enum::name)
                .sorted(Comparator.naturalOrder())
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.security().jwt().issuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long accessTokenTtlSeconds() {
        return properties.security().jwt().accessTokenTtlMinutes() * 60;
    }
}
