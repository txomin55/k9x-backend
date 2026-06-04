package com.k9x.infrastructure.in.rest.configuration.session.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.configuration.authentication.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class AuthTokenCacheConfiguration {

    @Bean
    Cache<String, UserInfoDTO> userInfoCache(SecurityProperties securityProperties) {
        return Caffeine.newBuilder()
                .expireAfterWrite(securityProperties.jwtCacheTtlMinutes(), TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    @Bean
    Cache<String, String> authTokenCache(
            Cache<String, UserInfoDTO> userInfoCache,
            SecurityProperties securityProperties) {
        return Caffeine.newBuilder()
                .expireAfterWrite(securityProperties.jwtCacheTtlMinutes(), TimeUnit.MINUTES)
                .maximumSize(10_000)
                .removalListener((String key, String _, RemovalCause _) -> userInfoCache.invalidate(key))
                .build();
    }
}
