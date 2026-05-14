package com.k9x.infrastructure.in.rest.endpoints.users;

import com.k9x.application.authentication.action.LoginServiceCase;
import com.k9x.application.authentication.command.LoginCommand;
import com.k9x.application.authentication.dto.LoginDTO;
import com.k9x.oas.stub.api.UsersLoginApiDelegate;
import com.k9x.oas.stub.model.LoginRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class Login implements UsersLoginApiDelegate {

    private final LoginServiceCase loginServiceCase;

    public Login(LoginServiceCase loginServiceCase) {
        this.loginServiceCase = loginServiceCase;
    }

    @Override
    public ResponseEntity<String> login(LoginRequestDTO loginDto) {
        LoginDTO result = loginServiceCase.login(new LoginCommand(loginDto.getCode()));
        return ResponseEntity.ok(result.jwtToken());
    }
}
