package com.k9x.infrastructure.in.rest.configuration.session.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.k9x.application.users.dto.UserInfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class AuthTokenCacheConfiguration {

    @Bean
    Cache<String, UserInfoDTO> userInfoCache(
            @Value("${k9x-backend.security.jwt-cache-ttl-minutes}") long ttlMinutes) {
        return Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    @Bean
    Cache<String, String> authTokenCache(
            Cache<String, UserInfoDTO> userInfoCache,
            @Value("${k9x-backend.security.jwt-cache-ttl-minutes}") long ttlMinutes) {
        return Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .removalListener((String key, String value, RemovalCause cause) -> userInfoCache.invalidate(key))
                .build();
    }
}
