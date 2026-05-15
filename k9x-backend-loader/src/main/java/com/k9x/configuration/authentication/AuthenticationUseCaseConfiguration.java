package com.k9x.configuration.authentication;

import com.k9x.application.users.port.ExchangeAuthorizationCodePort;
import com.k9x.application.users.port.JwtTokenCacheManagerPort;
import com.k9x.application.users.port.JwtTokenGeneratorPort;
import com.k9x.application.users.port.ValidateIdTokenPort;
import com.k9x.application.users.use_case.LoginServiceCase;
import com.k9x.infrastructure.out.rest.authentication.GoogleExchangeAuthorizationCodeAdapter;
import com.k9x.infrastructure.out.rest.authentication.GoogleValidateIdTokenAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationUseCaseConfiguration {

    @Bean
    public ExchangeAuthorizationCodePort exchangeAuthorizationCodePort(
            @Value("${k9x-backend.login.google.client_id}") String clientId,
            @Value("${k9x-backend.login.google.client_secret}") String clientSecret,
            @Value("${k9x-backend.login.google.redirect_url}") String redirectUri
    ) {
        return new GoogleExchangeAuthorizationCodeAdapter(
                clientId,
                clientSecret,
                redirectUri
        );
    }

    @Bean
    public ValidateIdTokenPort validateIdTokenPort(
            @Value("${k9x-backend.login.google.client_id}") String googleClientId
    ) {
        return new GoogleValidateIdTokenAdapter(googleClientId);
    }

    @Bean
    public LoginServiceCase loginServiceCase(
            ValidateIdTokenPort validateIdTokenPort,
            ExchangeAuthorizationCodePort exchangeAuthorizationCodePort,
            JwtTokenCacheManagerPort jwtTokenCacheManagerPort,
            JwtTokenGeneratorPort jwtTokenGeneratorPort,
            @Value("${k9x-backend.security.jwt-cache-ttl-minutes}") long ttlMinutes
    ) {
        return new LoginServiceCase(
                validateIdTokenPort,
                exchangeAuthorizationCodePort,
                jwtTokenCacheManagerPort,
                jwtTokenGeneratorPort,
                ttlMinutes
        );
    }
}
