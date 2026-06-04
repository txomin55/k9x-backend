package com.k9x.application.users.use_case;

import com.k9x.application.users.port.JwtTokenCacheManagerPort;
import com.k9x.application.users.port.ValidateRefreshTokenPort;
import com.k9x.application.users.use_case.dto.AuthTokenDTO;
import com.k9x.application.users.use_case.dto.LoginDTO;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

import java.util.Optional;

public class RefreshServiceCase {

    private final ValidateRefreshTokenPort validateRefreshTokenPort;
    private final JwtTokenCacheManagerPort jwtTokenCacheManagerPort;
    private final AccessTokenIssuer accessTokenIssuer;

    public RefreshServiceCase(
            ValidateRefreshTokenPort validateRefreshTokenPort,
            JwtTokenCacheManagerPort jwtTokenCacheManagerPort,
            AccessTokenIssuer accessTokenIssuer
    ) {
        this.validateRefreshTokenPort = validateRefreshTokenPort;
        this.jwtTokenCacheManagerPort = jwtTokenCacheManagerPort;
        this.accessTokenIssuer = accessTokenIssuer;
    }

    public LoginDTO refresh(String refreshToken) {
        Optional<String> userEmailOpt = validateRefreshTokenPort.getSubjectIfValid(refreshToken);
        if (userEmailOpt.isEmpty()) {
            throw new UnauthorizedResourceException();
        }
        String userEmail = userEmailOpt.get();

        AuthTokenDTO cachedData = jwtTokenCacheManagerPort.retrieveEntry(userEmail);
        int version = cachedData != null ? cachedData.getVersion() : 0;

        String jwtToken = accessTokenIssuer.issue(userEmail, version);

        return new LoginDTO(jwtToken, refreshToken);
    }
}
