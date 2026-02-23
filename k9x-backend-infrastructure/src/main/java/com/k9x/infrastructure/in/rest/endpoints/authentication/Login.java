package com.k9x.infrastructure.in.rest.endpoints.authentication;

import com.k9x.application.authentication.action.LoginServiceCase;
import com.k9x.application.authentication.command.LoginCommand;
import com.k9x.application.authentication.dto.LoginDTO;
import com.k9x.oas.stub.api.LoginApiDelegate;
import com.k9x.oas.stub.model.LoginWeb;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class Login implements LoginApiDelegate {

    private final LoginServiceCase loginServiceCase;

    public Login(LoginServiceCase loginServiceCase) {
        this.loginServiceCase = loginServiceCase;
    }

    @Override
    public ResponseEntity<String> login(LoginWeb loginWeb) {
        LoginDTO result = loginServiceCase.login(new LoginCommand(loginWeb.getIdToken()));
        return ResponseEntity.ok(result.jwtToken());
    }
}
