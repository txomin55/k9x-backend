package com.k9x.infrastructure.out.jwt;

import com.k9x.application.users.port.JwtTokenGeneratorPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

public class JwtTokenGeneratorAdapter implements JwtTokenGeneratorPort {

    private static final String ISSUER = "k9x-backend";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private final byte[] jwtSecret;

    public JwtTokenGeneratorAdapter(String jwtSecret) {
        this.jwtSecret = jwtSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String generate(String subject, int version, Duration ttl) {
        return build(subject, version, ttl, ACCESS_TOKEN_TYPE);
    }

    @Override
    public String generateRefreshToken(String subject, int version, Duration ttl) {
        return build(subject, version, ttl, REFRESH_TOKEN_TYPE);
    }

    private String build(String subject, int version, Duration ttl, String type) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + ttl.toMillis());
        return Jwts.builder()
                .issuer(ISSUER)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(Keys.hmacShaKeyFor(jwtSecret))
                .subject(subject)
                .audience().add(ISSUER).and()
                .claim("version", version)
                .claim("type", type)
                .compact();
    }
}
