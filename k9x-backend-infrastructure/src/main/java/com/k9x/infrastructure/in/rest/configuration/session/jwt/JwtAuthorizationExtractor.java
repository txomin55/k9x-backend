package com.k9x.infrastructure.in.rest.configuration.session.jwt;

import com.k9x.application.authentication.dto.AuthTokenDTO;
import com.k9x.infrastructure.in.rest.configuration.session.AuthorizationExtractor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class JwtAuthorizationExtractor implements AuthorizationExtractor {

    private final byte[] jwtSecret;

    public JwtAuthorizationExtractor(String jwtSecret) {
        this.jwtSecret = jwtSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public AuthTokenDTO getDataFromToken(String token) {

        Claims claimsJws = Jwts
                .parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subject = claimsJws.get("sub").toString();
        if (subject == null) {
            return null;
        }
        int version = Integer.parseInt(claimsJws.get("version").toString());
        Set<String> audiences = readAudiences(claimsJws.get("audience"));
        return new AuthTokenDTO(
                subject,
                claimsJws.getIssuer(),
                audiences,
                claimsJws.getIssuedAt(),
                version
        );
    }

    private Set<String> readAudiences(Object audClaim) {
        return switch (audClaim) {
            case null -> Set.of();
            case Set<?> audSet -> toStringSet(audSet);
            case Collection<?> audCollection -> toStringSet(audCollection);
            default -> Set.of(audClaim.toString());
        };
    }

    private Set<String> toStringSet(Collection<?> items) {
        Set<String> result = new LinkedHashSet<>();
        for (Object item : items) {
            if (item != null) {
                result.add(item.toString());
            }
        }
        return result;
    }
}
