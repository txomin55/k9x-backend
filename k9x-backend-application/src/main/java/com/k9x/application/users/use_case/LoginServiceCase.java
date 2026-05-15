package com.k9x.application.users.use_case;

import com.k9x.application.users.command.LoginCommand;
import com.k9x.application.users.dto.AuthTokenDTO;
import com.k9x.application.users.dto.LoginDTO;
import com.k9x.application.users.port.ExchangeAuthorizationCodePort;
import com.k9x.application.users.port.JwtTokenCacheManagerPort;
import com.k9x.application.users.port.JwtTokenGeneratorPort;
import com.k9x.application.users.port.ValidateIdTokenPort;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

import java.time.Duration;
import java.util.Optional;

public class LoginServiceCase {

    private final ValidateIdTokenPort validateIdTokenPort;
    private final ExchangeAuthorizationCodePort exchangeAuthorizationCodePort;
    private final JwtTokenCacheManagerPort jwtTokenCacheManagerPort;
    private final JwtTokenGeneratorPort jwtTokenGeneratorPort;
    private final Duration jwtTtl;

    public LoginServiceCase(
            ValidateIdTokenPort validateIdTokenPort,
            ExchangeAuthorizationCodePort exchangeAuthorizationCodePort,
            JwtTokenCacheManagerPort jwtTokenCacheManagerPort,
            JwtTokenGeneratorPort jwtTokenGeneratorPort,
            long ttlMinutes
    ) {
        this.validateIdTokenPort = validateIdTokenPort;
        this.exchangeAuthorizationCodePort = exchangeAuthorizationCodePort;
        this.jwtTokenCacheManagerPort = jwtTokenCacheManagerPort;
        this.jwtTokenGeneratorPort = jwtTokenGeneratorPort;
        this.jwtTtl = Duration.ofMinutes(ttlMinutes);
    }

    public LoginDTO login(LoginCommand command) {
        String code = command.idToken();
        Optional<String> exchangedIdTokenOpt = exchangeAuthorizationCodePort.exchangeForIdToken(code);
        if (exchangedIdTokenOpt.isEmpty()) {
            throw new UnauthorizedResourceException();
        }

        Optional<String> userEmailOpt = validateIdTokenPort.getEmailIfValid(exchangedIdTokenOpt.get());
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
