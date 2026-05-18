package com.k9x.infrastructure.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.k9x.application.users.port.JwtTokenCacheManagerPort;
import com.k9x.application.users.use_case.dto.AuthTokenDTO;
import com.k9x.infrastructure.in.rest.configuration.session.AuthorizationExtractor;

public class JwtTokenCacheManagerAdapter implements JwtTokenCacheManagerPort {

    private final Cache<String, String> authTokenCache;
    private final AuthorizationExtractor authorizationExtractor;

    public JwtTokenCacheManagerAdapter(
            Cache<String, String> authTokenCache,
            AuthorizationExtractor authorizationExtractor
    ) {
        this.authTokenCache = authTokenCache;
        this.authorizationExtractor = authorizationExtractor;
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
