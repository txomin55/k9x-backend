package com.k9x.infrastructure.in.rest.endpoints.secured.user;

import com.k9x.oas.stub.api.SecuredUserLogoutApiDelegate;
import org.springframework.http.ResponseEntity;

public class Logout implements SecuredUserLogoutApiDelegate {

    @Override
    public ResponseEntity<String> logoutSecured() {
        return ResponseEntity.ok("--MOCKED LOGOUT OK");
    }
}
