package com.k9x.infrastructure.in.rest.configuration.session.config;

import com.k9x.infrastructure.configuration.authentication.SecurityProperties;
import com.k9x.infrastructure.in.rest.configuration.session.RefreshTokenCookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RefreshCookieConfiguration {

    @Bean
    public RefreshTokenCookie refreshTokenCookie(SecurityProperties securityProperties) {
        return new RefreshTokenCookie(
                securityProperties.jwtRefreshTtlDays(),
                securityProperties.refreshCookie().sameSite(),
                securityProperties.refreshCookie().secure());
    }
}
