package com.k9x.infrastructure.out.jwt;

import com.k9x.application.users.port.ValidateRefreshTokenPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class JwtRefreshTokenValidatorAdapter implements ValidateRefreshTokenPort {

    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private final byte[] jwtSecret;

    public JwtRefreshTokenValidatorAdapter(String jwtSecret) {
        this.jwtSecret = jwtSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Optional<String> getSubjectIfValid(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts
                    .parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret))
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            Object type = claims.get("type");
            if (type == null || !REFRESH_TOKEN_TYPE.equals(type.toString())) {
                return Optional.empty();
            }

            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(subject);
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
