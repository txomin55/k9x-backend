package com.k9x.infrastructure.in.rest.endpoints.authentication;

import com.k9x.application.authentication.action.LoginServiceCase;
import com.k9x.application.authentication.command.LoginCommand;
import com.k9x.application.authentication.dto.LoginDTO;
import com.k9x.oas.stub.api.LoginApiDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class Login implements LoginApiDelegate {

    private static final String STATIC_ID_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImQyNzU0MDdjMzllODAzNmFhNzM1ZWIyYzE3YzU0ODc2MWNlZDZhNjQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiIzMjU1NTk0MDU1OS5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImF1ZCI6IjMyNTU1OTQwNTU5LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTA3MzY0NjIyNDE0MTc1MjUyMDY4IiwiZW1haWwiOiJ0eG9taW4uc2lyZXJhQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdF9oYXNoIjoiTDZoaEl3X2pvZWVJTTRJWE9XSC0zQSIsImlhdCI6MTc3MTg4MDg1NywiZXhwIjoxNzcxODg0NDU3fQ.ZZywdLRE2eQl7s5-dR4P00ctpEcagI2c2k_rfKkmWwdBXR2CbZ5a8cNYoN3njtK4skuzP3SgFoINk4ATXJTZ2bYl04FXNuuSXLZp_EtIrwxj_APz-lDsT1_HyP10ApOiJFdRWQa5irsMVC-mlHdCLOduXU30bgmloyawFccF7DcORSz7Z8hMolc7sliqAoZdPxRkNDI19sjftLQJGkVIYLNvGYChn8XfLV4CnQz8fqcltplnRl6LB9DRYjm0N9Pv3hjy0Vy89BBCuvKMXZNuLIwOTPlJr58b0Z-HpKGv1ZzZGFS5rUfy--kt6Feu16DvnUsw6QWtej5_Q6D-1lbqEA";
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
