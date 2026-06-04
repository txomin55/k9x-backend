package com.k9x.infrastructure.in.rest.configuration.session;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

/**
 * Centralises the {@code refresh_token} httpOnly cookie configuration so login, refresh and logout
 * all emit a consistent cookie. {@code Path=/refresh} makes the browser send it only to the refresh
 * endpoint. Locally: {@code SameSite=Lax}, not {@code Secure}. Production cross-domain:
 * {@code SameSite=None; Secure}.
 */
public class RefreshTokenCookie {

    public static final String NAME = "refresh_token";
    private static final String PATH = "/refresh";

    private final Duration maxAge;
    private final String sameSite;
    private final boolean secure;

    public RefreshTokenCookie(long refreshTtlDays, String sameSite, boolean secure) {
        this.maxAge = Duration.ofDays(refreshTtlDays);
        this.sameSite = sameSite;
        this.secure = secure;
    }

    public ResponseCookie create(String refreshToken) {
        return ResponseCookie.from(NAME, refreshToken)
                .httpOnly(true)
                .path(PATH)
                .sameSite(sameSite)
                .secure(secure)
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie clear() {
        return ResponseCookie.from(NAME, "")
                .httpOnly(true)
                .path(PATH)
                .sameSite(sameSite)
                .secure(secure)
                .maxAge(0)
                .build();
    }
}
