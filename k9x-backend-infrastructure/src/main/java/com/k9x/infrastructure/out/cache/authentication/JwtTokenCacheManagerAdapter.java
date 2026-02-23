package com.k9x.infrastructure.out.cache.authentication;

import com.github.benmanes.caffeine.cache.Cache;
import com.k9x.application.authentication.dto.AuthTokenDTO;
import com.k9x.application.authentication.port.JwtTokenCacheManagerPort;
import com.k9x.infrastructure.in.rest.configuration.session.jwt.JwtAuthorizationExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenCacheManagerAdapter implements JwtTokenCacheManagerPort {

    private final Cache<String, String> authTokenCache;
    private final JwtAuthorizationExtractor authorizationExtractor;

    public JwtTokenCacheManagerAdapter(
            Cache<String, String> authTokenCache,
            @Value("${k9x-backend.security.jwt-secret}") String jwtSecret
    ) {
        this.authTokenCache = authTokenCache;
        this.authorizationExtractor = new JwtAuthorizationExtractor(jwtSecret);
    }

    @Override
    public void overrideEntry(String id, String jwtToken) {
        authTokenCache.put(id, jwtToken);
    }

    @Override
    public AuthTokenDTO retrieveEntry(String id) {
        String token = authTokenCache.getIfPresent(id);
        if (token == null) {
            return null;
        }
        return authorizationExtractor.getDataFromToken(token);
    }
}
