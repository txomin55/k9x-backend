package com.k9x.infrastructure.in.rest.configuration.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.k9x.application.authentication.dto.AuthTokenDTO;
import com.k9x.infrastructure.in.rest.configuration.session.AuthorizationExtractor;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class Auth implements Filter {

    public static final String USER_DETAILS = "USER_DETAILS ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final AuthorizationExtractor authorizationExtractor;
    private final Cache<String, String> authTokenCache;
    private final Duration tokenTtl;
    public Auth(
            AuthorizationExtractor authorizationExtractor,
            Cache<String, String> authTokenCache,
            @Value("${k9x-backend.security.jwt-cache-ttl-minutes}") long ttlMinutes
    ) {
        this.authorizationExtractor = authorizationExtractor;
        this.authTokenCache = authTokenCache;
        this.tokenTtl = Duration.ofMinutes(ttlMinutes);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            chain.doFilter(req, res);
            return;
        }

        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            AuthTokenDTO userDetails = authorizationExtractor.getDataFromToken(authorization.split(" ")[1]);
            if (!isValidInCache(userDetails)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            request.setAttribute(USER_DETAILS, userDetails);

            chain.doFilter(req, res);
        } catch (RuntimeException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private boolean isValidInCache(AuthTokenDTO tokenDetails) {
        if (tokenDetails == null || tokenDetails.getSubject() == null) {
            return false;
        }

        String cached = authTokenCache.getIfPresent(tokenDetails.getSubject());
        if (cached == null) {
            return false;
        }

        AuthTokenDTO cachedDetails = authorizationExtractor.getDataFromToken(cached);
        if (cachedDetails == null) {
            return false;
        }

        if (equalsNullable(cachedDetails.getSubject(), tokenDetails.getSubject())) {
            return false;
        }

        if (cachedDetails.getVersion() != tokenDetails.getVersion()) {
            return false;
        }

        if (equalsNullable(cachedDetails.getIssuer(), tokenDetails.getIssuer())) {
            return false;
        }

        if (cachedDetails.getAudience() == null || tokenDetails.getAudience() == null) {
            return false;
        }
        if (!cachedDetails.getAudience().equals(tokenDetails.getAudience())) {
            return false;
        }

        Date issuedAt = cachedDetails.getIssuedAt();
        if (issuedAt == null) {
            return false;
        }
        Instant expiresAt = issuedAt.toInstant().plus(tokenTtl);
        return Instant.now().isBefore(expiresAt);
    }

    private boolean equalsNullable(String left, String right) {
        if (left == null) {
            return right != null;
        }
        return !left.equals(right);
    }
}
