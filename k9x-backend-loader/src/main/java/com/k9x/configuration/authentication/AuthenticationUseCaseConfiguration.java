package com.k9x.configuration.authentication;

import com.k9x.application.users.port.CreateUserPersistencePort;
import com.k9x.application.users.port.ExchangeAuthorizationCodePort;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.application.users.port.JwtTokenCacheManagerPort;
import com.k9x.application.users.port.JwtTokenGeneratorPort;
import com.k9x.application.users.port.ValidateIdTokenPort;
import com.k9x.application.users.port.ValidateRefreshTokenPort;
import com.k9x.application.users.use_case.AccessTokenIssuer;
import com.k9x.application.users.use_case.LoginServiceCase;
import com.k9x.application.users.use_case.LogoutServiceCase;
import com.k9x.application.users.use_case.RefreshServiceCase;
import com.k9x.infrastructure.configuration.authentication.SecurityProperties;
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
    public LogoutServiceCase logoutServiceCase(JwtTokenCacheManagerPort jwtTokenCacheManagerPort) {
        return new LogoutServiceCase(jwtTokenCacheManagerPort);
    }

    @Bean
    public AccessTokenIssuer accessTokenIssuer(
            JwtTokenGeneratorPort jwtTokenGeneratorPort,
            JwtTokenCacheManagerPort jwtTokenCacheManagerPort,
            SecurityProperties securityProperties
    ) {
        return new AccessTokenIssuer(jwtTokenGeneratorPort, jwtTokenCacheManagerPort, securityProperties.jwtCacheTtlMinutes());
    }

    @Bean
    public LoginServiceCase loginServiceCase(
            ValidateIdTokenPort validateIdTokenPort,
            ExchangeAuthorizationCodePort exchangeAuthorizationCodePort,
            JwtTokenCacheManagerPort jwtTokenCacheManagerPort,
            JwtTokenGeneratorPort jwtTokenGeneratorPort,
            GetUserInfoPersistencePort getUserInfoPersistencePort,
            CreateUserPersistencePort createUserPersistencePort,
            AccessTokenIssuer accessTokenIssuer,
            SecurityProperties securityProperties
    ) {
        return new LoginServiceCase(
                validateIdTokenPort,
                exchangeAuthorizationCodePort,
                jwtTokenCacheManagerPort,
                jwtTokenGeneratorPort,
                getUserInfoPersistencePort,
                createUserPersistencePort,
                accessTokenIssuer,
                securityProperties.jwtRefreshTtlDays()
        );
    }

    @Bean
    public RefreshServiceCase refreshServiceCase(
            ValidateRefreshTokenPort validateRefreshTokenPort,
            JwtTokenCacheManagerPort jwtTokenCacheManagerPort,
            AccessTokenIssuer accessTokenIssuer
    ) {
        return new RefreshServiceCase(
                validateRefreshTokenPort,
                jwtTokenCacheManagerPort,
                accessTokenIssuer
        );
    }
}
