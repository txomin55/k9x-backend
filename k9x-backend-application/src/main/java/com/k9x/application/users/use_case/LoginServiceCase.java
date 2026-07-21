package com.k9x.application.users.use_case;

import com.k9x.application.users.port.*;
import com.k9x.application.users.use_case.command.LoginCommand;
import com.k9x.application.users.use_case.dto.AuthTokenDTO;
import com.k9x.application.users.use_case.dto.LoginDTO;
import com.k9x.application.users.use_case.dto.ValidatedIdTokenDTO;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

import java.time.Duration;
import java.util.Optional;

public class LoginServiceCase implements TransactionalUseCase {

    private final ValidateIdTokenPort validateIdTokenPort;
    private final ExchangeAuthorizationCodePort exchangeAuthorizationCodePort;
    private final JwtTokenCacheManagerPort jwtTokenCacheManagerPort;
    private final JwtTokenGeneratorPort jwtTokenGeneratorPort;
    private final GetUserInfoPersistencePort getUserInfoPersistencePort;
    private final CreateUserPersistencePort createUserPersistencePort;
    private final AccessTokenIssuer accessTokenIssuer;
    private final Duration refreshTtl;

    public LoginServiceCase(
            ValidateIdTokenPort validateIdTokenPort,
            ExchangeAuthorizationCodePort exchangeAuthorizationCodePort,
            JwtTokenCacheManagerPort jwtTokenCacheManagerPort,
            JwtTokenGeneratorPort jwtTokenGeneratorPort,
            GetUserInfoPersistencePort getUserInfoPersistencePort,
            CreateUserPersistencePort createUserPersistencePort,
            AccessTokenIssuer accessTokenIssuer,
            long refreshTtlDays
    ) {
        this.validateIdTokenPort = validateIdTokenPort;
        this.exchangeAuthorizationCodePort = exchangeAuthorizationCodePort;
        this.jwtTokenCacheManagerPort = jwtTokenCacheManagerPort;
        this.jwtTokenGeneratorPort = jwtTokenGeneratorPort;
        this.getUserInfoPersistencePort = getUserInfoPersistencePort;
        this.createUserPersistencePort = createUserPersistencePort;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTtl = Duration.ofDays(refreshTtlDays);
    }

    public LoginDTO login(LoginCommand command) {
        String code = command.code();
        Optional<String> exchangedIdTokenOpt = exchangeAuthorizationCodePort.exchangeForIdToken(code);
        if (exchangedIdTokenOpt.isEmpty()) {
            throw new UnauthorizedResourceException();
        }

        Optional<ValidatedIdTokenDTO> validatedTokenOpt = validateIdTokenPort.getUserIfValid(exchangedIdTokenOpt.get());
        if (validatedTokenOpt.isEmpty()) {
            throw new UnauthorizedResourceException();
        }
        ValidatedIdTokenDTO validatedToken = validatedTokenOpt.get();
        String userEmail = validatedToken.email();

        AuthTokenDTO cachedData = jwtTokenCacheManagerPort.retrieveEntry(userEmail);
        int version = cachedData != null ? cachedData.getVersion() + 1 : 0;

        String jwtToken = accessTokenIssuer.issue(userEmail, version);
        String refreshToken = jwtTokenGeneratorPort.generateRefreshToken(userEmail, version, refreshTtl);

        if (getUserInfoPersistencePort.findById(userEmail) == null) {
            createUserPersistencePort.createUser(userEmail, validatedToken.image());
        }

        return new LoginDTO(jwtToken, refreshToken);
    }

}
