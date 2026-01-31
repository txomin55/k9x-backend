package com.k9x.infrastructure.in.rest.endpoints.authentication;

import com.k9x.oas.stub.api.LoginApiDelegate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class Login implements LoginApiDelegate {

    @Override
    public ResponseEntity<String> login() {
        return ResponseEntity.ok("--MOCKED LOGIN OK");
    }
}
