package com.k9x.configuration.authentication;

import com.k9x.application.authentication.action.LoginServiceCase;
import com.k9x.application.authentication.port.JwtTokenCacheManagerPort;
import com.k9x.application.authentication.port.JwtTokenGeneratorPort;
import com.k9x.application.authentication.port.ValidateIdTokenPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationUseCaseConfiguration {

    @Bean
    public LoginServiceCase loginServiceCase(
            ValidateIdTokenPort validateIdTokenPort,
            JwtTokenCacheManagerPort jwtTokenCacheManagerPort,
            JwtTokenGeneratorPort jwtTokenGeneratorPort,
            @Value("${k9x-backend.security.jwt-cache-ttl-minutes}") long ttlMinutes
    ) {
        return new LoginServiceCase(
                validateIdTokenPort,
                jwtTokenCacheManagerPort,
                jwtTokenGeneratorPort,
                ttlMinutes
        );
    }
}
