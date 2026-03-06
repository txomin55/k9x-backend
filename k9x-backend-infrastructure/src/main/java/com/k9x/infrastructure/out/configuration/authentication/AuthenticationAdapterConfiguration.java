package com.k9x.infrastructure.out.configuration.authentication;

import com.github.benmanes.caffeine.cache.Cache;
import com.k9x.application.authentication.port.JwtTokenCacheManagerPort;
import com.k9x.application.authentication.port.JwtTokenGeneratorPort;
import com.k9x.infrastructure.in.rest.configuration.session.AuthorizationExtractor;
import com.k9x.infrastructure.out.cache.authentication.JwtTokenCacheManagerAdapter;
import com.k9x.infrastructure.out.jwt.JwtTokenGeneratorAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationAdapterConfiguration {

    @Bean
    public JwtTokenGeneratorPort jwtTokenGeneratorPort(
            @Value("${k9x-backend.security.jwt-secret}") String jwtSecret
    ) {
        return new JwtTokenGeneratorAdapter(jwtSecret);
    }

    @Bean
    public JwtTokenCacheManagerPort jwtTokenCacheManagerPort(
            Cache<String, String> authTokenCache,
            AuthorizationExtractor authorizationExtractor
    ) {
        return new JwtTokenCacheManagerAdapter(authTokenCache, authorizationExtractor);
    }
}
