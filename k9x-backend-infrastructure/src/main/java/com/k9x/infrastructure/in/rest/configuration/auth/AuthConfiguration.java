package com.k9x.infrastructure.in.rest.configuration.auth;

import com.k9x.infrastructure.in.rest.configuration.session.user.AuthorizationExtractor;
import com.k9x.infrastructure.in.rest.configuration.session.user.type.jwt.JwtAuthorizationExtractor;
import com.k9x.infrastructure.in.rest.configuration.session.user.type.mocked.MockedAuthorizationExtractor;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {

    Logger logger = LoggerFactory.getLogger(AuthConfiguration.class);

    @Bean
    @ConditionalOnProperty(value = "k9x-backend.auth.mocked", havingValue = "true")
    AuthorizationExtractor mockExtractor() {
        logger.warn("YOU ARE SETTING UP THE REST API WITH MOCKED JWT VALUE");
        return new MockedAuthorizationExtractor("1", "Txomin Sirera", Collections.singletonList("k9x"));
    }

    @Bean
    @ConditionalOnProperty(value = "k9x-backend.auth.mocked", havingValue = "false")
    AuthorizationExtractor jwtExtractor() {
        return new JwtAuthorizationExtractor();
    }
}
