package com.k9x.infrastructure.in.rest.endpoints.secured.users;

import com.k9x.application.users.use_case.LogoutServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.infrastructure.in.rest.configuration.session.RefreshTokenCookie;
import com.k9x.oas.stub.api.SecuredUserLogoutApiDelegate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class Logout implements SecuredUserLogoutApiDelegate {

    private final LogoutServiceCase logoutServiceCase;
    private final UserInfoDTO userDetails;
    private final RefreshTokenCookie refreshTokenCookie;

    public Logout(LogoutServiceCase logoutServiceCase, UserInfoDTO userDetails, RefreshTokenCookie refreshTokenCookie) {
        this.logoutServiceCase = logoutServiceCase;
        this.userDetails = userDetails;
        this.refreshTokenCookie = refreshTokenCookie;
    }

    @Override
    public ResponseEntity<String> logoutSecured() {
        logoutServiceCase.logout(userDetails.getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.clear().toString())
                .build();
    }
}
