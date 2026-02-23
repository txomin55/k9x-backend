package com.k9x.infrastructure.in.rest.configuration.session.config;

import com.k9x.infrastructure.in.rest.configuration.session.AuthorizationExtractor;
import com.k9x.infrastructure.in.rest.configuration.session.jwt.JwtAuthorizationExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {

    @Bean
    AuthorizationExtractor jwtExtractor(@Value("${k9x-backend.security.jwt-secret}") String jwtSecret) {
        return new JwtAuthorizationExtractor(jwtSecret);
    }
}
