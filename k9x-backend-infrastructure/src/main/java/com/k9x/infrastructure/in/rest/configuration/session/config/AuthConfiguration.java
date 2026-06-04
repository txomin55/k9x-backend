package com.k9x.infrastructure.in.rest.configuration.session.config;

import com.k9x.infrastructure.configuration.authentication.SecurityProperties;
import com.k9x.infrastructure.in.rest.configuration.session.AuthorizationExtractor;
import com.k9x.infrastructure.in.rest.configuration.session.jwt.JwtAuthorizationExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {

    @Bean
    AuthorizationExtractor jwtExtractor(SecurityProperties securityProperties) {
        return new JwtAuthorizationExtractor(securityProperties.jwtSecret());
    }
}
