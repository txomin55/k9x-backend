package com.k9x.application.users.use_case;

import com.k9x.application.users.port.JwtTokenCacheManagerPort;
import com.k9x.application.users.port.JwtTokenGeneratorPort;

import java.time.Duration;

/**
 * Issues an access token for a user: generates the JWT and registers it in the token cache so
 * {@code Auth.isValidInCache} accepts it. Shared by login and refresh; the version policy
 * (increment on login, reuse on refresh) stays with the caller.
 */
public class AccessTokenIssuer {

    private final JwtTokenGeneratorPort jwtTokenGeneratorPort;
    private final JwtTokenCacheManagerPort jwtTokenCacheManagerPort;
    private final Duration jwtTtl;

    public AccessTokenIssuer(
            JwtTokenGeneratorPort jwtTokenGeneratorPort,
            JwtTokenCacheManagerPort jwtTokenCacheManagerPort,
            long ttlMinutes
    ) {
        this.jwtTokenGeneratorPort = jwtTokenGeneratorPort;
        this.jwtTokenCacheManagerPort = jwtTokenCacheManagerPort;
        this.jwtTtl = Duration.ofMinutes(ttlMinutes);
    }

    public String issue(String userEmail, int version) {
        String token = jwtTokenGeneratorPort.generate(userEmail, version, jwtTtl);
        jwtTokenCacheManagerPort.overrideEntry(userEmail, token);
        return token;
    }
}