package com.k9x.infrastructure.in.rest.endpoints.authentication;

import com.k9x.application.authentication.action.LoginServiceCase;
import com.k9x.application.authentication.command.LoginCommand;
import com.k9x.application.authentication.dto.LoginDTO;
import com.k9x.oas.stub.api.LoginApiDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class Login implements LoginApiDelegate {

    private static final String STATIC_ID_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImQyNzU0MDdjMzllODAzNmFhNzM1ZWIyYzE3YzU0ODc2MWNlZDZhNjQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiIzMjU1NTk0MDU1OS5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImF1ZCI6IjMyNTU1OTQwNTU5LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTA3MzY0NjIyNDE0MTc1MjUyMDY4IiwiZW1haWwiOiJ0eG9taW4uc2lyZXJhQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdF9oYXNoIjoiQlRtTUZVRUc1eVMzdF9IcGtncmVZZyIsImlhdCI6MTc3MTg4NjM4NCwiZXhwIjoxNzcxODg5OTg0fQ.QSQpk2AN5PdMWDBE3wAfCaGnUnpM9Z3KgJqO1sprSQ3LB6yMEgjHtvfVsp9XiD7cxfaQXtO2TFfx3urDJxkT2dhELwzOfl09unSpIXYei4qskKR_eKQWhZ4hYWi1S1u4niHnFxEdedlGEDZM6SMlCZ0Om9lrXTm3kyAakVXEMCk653DyxKryK4GJ9iF4EMWnMtapPnm5o41_3GiGVG82gpIgkc_PLiHzHzB32wCwv1bO4qtNVWcL9OcRSA9bMF6XqD_nnScZ9EeapMNA59WwHd5p0mSyvIzFAhiCHU182A1id-ty6qmb_HIfi0MZExewK0i5XuRPTIuVCZznH6aU_Q";
    private final LoginServiceCase loginServiceCase;

    public Login(LoginServiceCase loginServiceCase) {
        this.loginServiceCase = loginServiceCase;
    }

    @Override
    public ResponseEntity<String> login() {
        LoginDTO result = loginServiceCase.login(new LoginCommand(STATIC_ID_TOKEN));
        return ResponseEntity.ok(result.jwtToken());
    }
}
