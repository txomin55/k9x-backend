package com.k9x.infrastructure.in.rest.endpoints.secured.users;

import com.k9x.application.users.use_case.LogoutServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredUserLogoutApiDelegate;
import org.springframework.http.ResponseEntity;

public class Logout implements SecuredUserLogoutApiDelegate {

    private final LogoutServiceCase logoutServiceCase;
    private final UserInfoDTO userDetails;

    public Logout(LogoutServiceCase logoutServiceCase, UserInfoDTO userDetails) {
        this.logoutServiceCase = logoutServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> logoutSecured() {
        logoutServiceCase.logout(userDetails.getEmail());
        return ResponseEntity.ok().build();
    }
}
