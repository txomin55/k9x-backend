package com.k9x.infrastructure.in.rest.configuration.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.application.users.use_case.dto.AuthTokenDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.configuration.authentication.SecurityProperties;
import com.k9x.infrastructure.in.rest.configuration.session.AuthorizationExtractor;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class Auth implements Filter {

    private static final Logger log = LoggerFactory.getLogger(Auth.class);

    public static final String USER_DETAILS = "USER_DETAILS";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final AuthorizationExtractor authorizationExtractor;
    private final Cache<String, String> authTokenCache;
    private final Cache<String, UserInfoDTO> userInfoCache;
    private final GetUserInfoPersistencePort getUserInfoPort;
    private final Duration tokenTtl;

    public Auth(
            AuthorizationExtractor authorizationExtractor,
            Cache<String, String> authTokenCache,
            Cache<String, UserInfoDTO> userInfoCache,
            GetUserInfoPersistencePort getUserInfoPort,
            SecurityProperties securityProperties
    ) {
        this.authorizationExtractor = authorizationExtractor;
        this.authTokenCache = authTokenCache;
        this.userInfoCache = userInfoCache;
        this.getUserInfoPort = getUserInfoPort;
        this.tokenTtl = Duration.ofMinutes(securityProperties.jwtCacheTtlMinutes());
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        if (!path.startsWith("/secured/")) {
            // Public paths authenticate optionally: if a token happens to come along it is resolved so the
            // endpoint can tell who is asking, but a missing or invalid one is never rejected.
            resolveOptionalUser(request);
            chain.doFilter(req, res);
            return;
        }

        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null) {
            log.warn("Auth 401 [{} {}]: missing Authorization header", request.getMethod(), path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            AuthTokenDTO userDetails = authorizationExtractor.getDataFromToken(authorization.split(" ")[1]);
            if (!isValidInCache(userDetails)) {
                log.warn("Auth 401 [{} {}]: token not valid in cache (subject={})",
                        request.getMethod(), path, userDetails != null ? userDetails.getSubject() : null);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            UserInfoDTO userInfo = findUser(userDetails.getSubject());

            if (userInfo == null) {
                log.warn("Auth 401 [{} {}]: no user found for subject={}",
                        request.getMethod(), path, userDetails.getSubject());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            request.setAttribute(USER_DETAILS, userInfo);

            chain.doFilter(req, res);
        } catch (RuntimeException ex) {
            log.warn("Auth 401 [{} {}]: {}", request.getMethod(), path, ex.toString());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * Best-effort resolution for public paths: sets the user when a valid token is present and stays silent
     * otherwise, so an anonymous request is indistinguishable from one with a stale token.
     */
    private void resolveOptionalUser(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.contains(" ")) {
            return;
        }
        try {
            AuthTokenDTO tokenDetails = authorizationExtractor.getDataFromToken(authorization.split(" ")[1]);
            if (!isValidInCache(tokenDetails)) {
                return;
            }
            UserInfoDTO userInfo = findUser(tokenDetails.getSubject());
            if (userInfo != null) {
                request.setAttribute(USER_DETAILS, userInfo);
            }
        } catch (RuntimeException ex) {
            log.debug("Optional auth skipped [{}]: {}", request.getRequestURI(), ex.toString());
        }
    }

    private UserInfoDTO findUser(String subject) {
        UserInfoDTO cached = userInfoCache.getIfPresent(subject);
        if (cached != null) {
            return cached;
        }
        UserInfoDTO userInfo = getUserInfoPort.findById(subject);
        if (userInfo != null) {
            userInfoCache.put(subject, userInfo);
        }
        return userInfo;
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

        if (!Objects.equals(cachedDetails.getSubject(), tokenDetails.getSubject())) {
            return false;
        }

        if (cachedDetails.getVersion() != tokenDetails.getVersion()) {
            return false;
        }

        if (!Objects.equals(cachedDetails.getIssuer(), tokenDetails.getIssuer())) {
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

}
