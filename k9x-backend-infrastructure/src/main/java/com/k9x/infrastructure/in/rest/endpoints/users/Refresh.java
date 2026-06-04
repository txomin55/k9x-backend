package com.k9x.infrastructure.in.rest.endpoints.users;

import com.k9x.application.users.use_case.RefreshServiceCase;
import com.k9x.application.users.use_case.dto.LoginDTO;
import com.k9x.infrastructure.in.rest.configuration.session.RefreshTokenCookie;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public {@code POST /refresh} endpoint. Lives outside {@code /secured/} so the {@code Auth} filter
 * skips it: the access token is expired here and the httpOnly {@code refresh_token} cookie authorises.
 *
 * <p>Hand-written so the flow can be tested locally before {@code /refresh} is added to the external
 * OAS definition. The refresh token is read from the request (not as a method parameter) so the
 * method signature matches the future OAS-generated delegate; once the stub exists, swap the
 * {@code @RestController}/{@code @PostMapping} for {@code implements …RefreshApiDelegate} keeping the body.
 */
@RestController
public class Refresh {

    public static final String NAME = "refresh_token";

    private final RefreshServiceCase refreshServiceCase;
    private final RefreshTokenCookie refreshTokenCookie;
    private final HttpServletRequest request;

    public Refresh(RefreshServiceCase refreshServiceCase, RefreshTokenCookie refreshTokenCookie, HttpServletRequest request) {
        this.refreshServiceCase = refreshServiceCase;
        this.refreshTokenCookie = refreshTokenCookie;
        this.request = request;
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh() {
        LoginDTO result = refreshServiceCase.refresh(read(request));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(result.refreshToken()).toString())
                .body(result.jwtToken());
    }

    private String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
