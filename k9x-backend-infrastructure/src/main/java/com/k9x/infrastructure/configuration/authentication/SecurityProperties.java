package com.k9x.infrastructure.configuration.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed binding of the {@code k9x-backend.security} configuration block. Single source for the JWT
 * secret, token TTLs and the refresh cookie attributes, so they are not scattered across {@code @Value}
 * annotations. Relaxed binding maps {@code jwt-secret} → {@code jwtSecret}, etc.
 */
@ConfigurationProperties(prefix = "k9x-backend.security")
public record SecurityProperties(
        String jwtSecret,
        long jwtCacheTtlMinutes,
        long jwtRefreshTtlDays,
        RefreshCookie refreshCookie
) {

    public record RefreshCookie(String sameSite, boolean secure) {
    }
}
