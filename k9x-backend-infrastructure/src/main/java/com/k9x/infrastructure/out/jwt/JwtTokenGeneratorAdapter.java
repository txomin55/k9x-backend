package com.k9x.infrastructure.out.jwt;

import com.k9x.application.users.port.JwtTokenGeneratorPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

public class JwtTokenGeneratorAdapter implements JwtTokenGeneratorPort {

    private static final String ISSUER = "k9x-backend";
    private final byte[] jwtSecret;

    public JwtTokenGeneratorAdapter(String jwtSecret) {
        this.jwtSecret = jwtSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String generate(String subject, int version, Duration ttl) {
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
                .compact();
    }
}
