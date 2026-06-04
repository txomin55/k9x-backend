package com.k9x.application.users.use_case;

import com.k9x.application.users.port.JwtTokenCacheManagerPort;
import com.k9x.application.users.port.JwtTokenGeneratorPort;
import com.k9x.application.users.port.ValidateRefreshTokenPort;
import com.k9x.application.users.use_case.dto.AuthTokenDTO;
import com.k9x.application.users.use_case.dto.LoginDTO;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshServiceCaseTest {

    @Mock
    private ValidateRefreshTokenPort validateRefreshTokenPort;

    @Mock
    private JwtTokenCacheManagerPort jwtTokenCacheManagerPort;

    @Mock
    private JwtTokenGeneratorPort jwtTokenGeneratorPort;

    private RefreshServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        AccessTokenIssuer accessTokenIssuer =
                new AccessTokenIssuer(jwtTokenGeneratorPort, jwtTokenCacheManagerPort, 15L);
        serviceCase = new RefreshServiceCase(
                validateRefreshTokenPort,
                jwtTokenCacheManagerPort,
                accessTokenIssuer
        );
    }

    @Test
    void throws_exception_when_refresh_token_is_invalid() {
        when(validateRefreshTokenPort.getSubjectIfValid("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceCase.refresh("bad-token"))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(jwtTokenCacheManagerPort, jwtTokenGeneratorPort);
    }

    @Test
    void generates_access_token_with_version_0_when_no_cached_entry() {
        when(validateRefreshTokenPort.getSubjectIfValid("refresh-token")).thenReturn(Optional.of("user@test.com"));
        when(jwtTokenCacheManagerPort.retrieveEntry("user@test.com")).thenReturn(null);
        when(jwtTokenGeneratorPort.generate(eq("user@test.com"), eq(0), any())).thenReturn("new-access-token");

        LoginDTO result = serviceCase.refresh("refresh-token");

        assertThat(result.jwtToken()).isEqualTo("new-access-token");
        verify(jwtTokenCacheManagerPort).overrideEntry("user@test.com", "new-access-token");
    }

    @Test
    void reuses_cached_version_without_incrementing() {
        AuthTokenDTO cached = new AuthTokenDTO("user@test.com", "issuer", null, null, 3);
        when(validateRefreshTokenPort.getSubjectIfValid("refresh-token")).thenReturn(Optional.of("user@test.com"));
        when(jwtTokenCacheManagerPort.retrieveEntry("user@test.com")).thenReturn(cached);
        when(jwtTokenGeneratorPort.generate(eq("user@test.com"), eq(3), any())).thenReturn("new-access-token-v3");

        LoginDTO result = serviceCase.refresh("refresh-token");

        assertThat(result.jwtToken()).isEqualTo("new-access-token-v3");
        verify(jwtTokenGeneratorPort).generate(eq("user@test.com"), eq(3), any());
        verify(jwtTokenCacheManagerPort).overrideEntry("user@test.com", "new-access-token-v3");
    }
}
