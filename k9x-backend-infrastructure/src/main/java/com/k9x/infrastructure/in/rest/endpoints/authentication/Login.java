package com.k9x.infrastructure.in.rest.endpoints.authentication;

import com.k9x.application.authentication.action.LoginServiceCase;
import com.k9x.application.authentication.command.LoginCommand;
import com.k9x.application.authentication.dto.LoginDTO;
import com.k9x.oas.stub.api.LoginApiDelegate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class Login implements LoginApiDelegate {

    private static final String STATIC_ID_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImQyNzU0MDdjMzllODAzNmFhNzM1ZWIyYzE3YzU0ODc2MWNlZDZhNjQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiIzMjU1NTk0MDU1OS5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImF1ZCI6IjMyNTU1OTQwNTU5LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTA3MzY0NjIyNDE0MTc1MjUyMDY4IiwiZW1haWwiOiJ0eG9taW4uc2lyZXJhQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdF9oYXNoIjoiUjhmeW9ndkY2RWdvRWFrczkzcjhCUSIsImlhdCI6MTc3MTg3NTM2OSwiZXhwIjoxNzcxODc4OTY5fQ.nDlnum_UMumt2ovPSpWO35WfZ8moMTBg8P2_6el-Wgu_o6_YBgkAc3-3_EiEbGP6ffPYnmJ04h7sVbSXm9OkOtv1TT329GxatGiQPT7gChxcUVH3KA0XXNWPrT2sW0SVzqwmPUCSLfRiJiSKI4bszVm4ORabtTtNM6ORW0MUcoDLfZDFgC8jNVFeedLW_WXGGGkrnoMsJBgNwLjhWdYm6Ahe3PVEpr8-xjpPNHP3dyHXhvQBz4k93N4Ruvo3WJItDgsZd4HSS8yxSngF8NzSxO651xdd7SBWX0644wsMal4-3AmxenHamz4MX-0Y7jkBpfhTyGzH9oMCxhUCPMWg7A";
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
