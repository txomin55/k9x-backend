package com.k9x.application.authentication.action;

import com.k9x.application.authentication.command.LoginCommand;
import com.k9x.application.authentication.dto.AuthTokenDTO;
import com.k9x.application.authentication.dto.LoginDTO;
import com.k9x.application.authentication.port.JwtTokenGeneratorPort;
import com.k9x.application.authentication.port.JwtTokenCacheManagerPort;
import com.k9x.application.authentication.port.ValidateIdTokenPort;
import com.k9x.domain.commons.exception.UnauthorizedResourceException;

import java.time.Duration;
import java.util.Optional;

public class LoginServiceCase {

    private final ValidateIdTokenPort validateIdTokenPort;
    private final JwtTokenCacheManagerPort jwtTokenCacheManagerPort;
    private final JwtTokenGeneratorPort jwtTokenGeneratorPort;
    private final Duration jwtTtl;

    public LoginServiceCase(
            ValidateIdTokenPort validateIdTokenPort,
            JwtTokenCacheManagerPort jwtTokenCacheManagerPort,
            JwtTokenGeneratorPort jwtTokenGeneratorPort,
            long ttlMinutes
    ) {
        this.validateIdTokenPort = validateIdTokenPort;
        this.jwtTokenCacheManagerPort = jwtTokenCacheManagerPort;
        this.jwtTokenGeneratorPort = jwtTokenGeneratorPort;
        this.jwtTtl = Duration.ofMinutes(ttlMinutes);
    }

    public LoginDTO login(LoginCommand command) {
        Optional<String> userEmailOpt = validateIdTokenPort.getEmailIfValid(command.idToken());
        if (userEmailOpt.isEmpty()) {
            throw new UnauthorizedResourceException();
        }
        String userEmail = userEmailOpt.get();

        AuthTokenDTO cachedData = jwtTokenCacheManagerPort.retrieveEntry(userEmail);

        int version = cachedData != null ? cachedData.getVersion() + 1 : 0;
        String jwtToken = jwtTokenGeneratorPort.generate(userEmail, version, jwtTtl);

        jwtTokenCacheManagerPort.overrideEntry(userEmail, jwtToken);

        return new LoginDTO(true, jwtToken);
    }

}
