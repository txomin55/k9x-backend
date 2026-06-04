package com.k9x.infrastructure.configuration.authentication;

import com.github.benmanes.caffeine.cache.Cache;
import com.k9x.application.users.port.JwtTokenCacheManagerPort;
import com.k9x.application.users.port.JwtTokenGeneratorPort;
import com.k9x.application.users.port.ValidateRefreshTokenPort;
import com.k9x.infrastructure.in.rest.configuration.session.AuthorizationExtractor;
import com.k9x.infrastructure.out.cache.JwtTokenCacheManagerAdapter;
import com.k9x.infrastructure.out.jwt.JwtRefreshTokenValidatorAdapter;
import com.k9x.infrastructure.out.jwt.JwtTokenGeneratorAdapter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class AuthenticationAdapterConfiguration {

    @Bean
    public JwtTokenGeneratorPort jwtTokenGeneratorPort(SecurityProperties securityProperties) {
        return new JwtTokenGeneratorAdapter(securityProperties.jwtSecret());
    }

    @Bean
    public ValidateRefreshTokenPort validateRefreshTokenPort(SecurityProperties securityProperties) {
        return new JwtRefreshTokenValidatorAdapter(securityProperties.jwtSecret());
    }

    @Bean
    public JwtTokenCacheManagerPort jwtTokenCacheManagerPort(
            Cache<String, String> authTokenCache,
            AuthorizationExtractor authorizationExtractor
    ) {
        return new JwtTokenCacheManagerAdapter(authTokenCache, authorizationExtractor);
    }
}
