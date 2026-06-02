package com.k9x.application.users.use_case;

import com.k9x.application.users.port.CreateUserPersistencePort;
import com.k9x.application.users.port.ExchangeAuthorizationCodePort;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.application.users.port.JwtTokenCacheManagerPort;
import com.k9x.application.users.port.JwtTokenGeneratorPort;
import com.k9x.application.users.port.ValidateIdTokenPort;
import com.k9x.application.users.use_case.command.LoginCommand;
import com.k9x.application.users.use_case.dto.AuthTokenDTO;
import com.k9x.application.users.use_case.dto.LoginDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.application.users.use_case.dto.ValidatedIdTokenDTO;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceCaseTest {

    @Mock
    private ValidateIdTokenPort validateIdTokenPort;

    @Mock
    private ExchangeAuthorizationCodePort exchangeAuthorizationCodePort;

    @Mock
    private JwtTokenCacheManagerPort jwtTokenCacheManagerPort;

    @Mock
    private JwtTokenGeneratorPort jwtTokenGeneratorPort;

    @Mock
    private GetUserInfoPersistencePort getUserInfoPersistencePort;

    @Mock
    private CreateUserPersistencePort createUserPersistencePort;

    private LoginServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new LoginServiceCase(
                validateIdTokenPort,
                exchangeAuthorizationCodePort,
                jwtTokenCacheManagerPort,
                jwtTokenGeneratorPort,
                getUserInfoPersistencePort,
                createUserPersistencePort,
                60L
        );
    }

    @Test
    void throws_exception_when_code_exchange_fails() {
        when(exchangeAuthorizationCodePort.exchangeForIdToken("code-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceCase.login(new LoginCommand("code-1")))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(validateIdTokenPort, jwtTokenCacheManagerPort, jwtTokenGeneratorPort);
    }

    @Test
    void throws_exception_when_id_token_is_invalid() {
        when(exchangeAuthorizationCodePort.exchangeForIdToken("code-1")).thenReturn(Optional.of("id-token"));
        when(validateIdTokenPort.getUserIfValid("id-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceCase.login(new LoginCommand("code-1")))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(jwtTokenCacheManagerPort, jwtTokenGeneratorPort);
    }

    @Test
    void returns_jwt_token_with_version_0_when_no_cached_token() {
        when(exchangeAuthorizationCodePort.exchangeForIdToken("code-1")).thenReturn(Optional.of("id-token"));
        when(validateIdTokenPort.getUserIfValid("id-token")).thenReturn(Optional.of(new ValidatedIdTokenDTO("user@test.com", "http://img/u.png")));
        when(jwtTokenCacheManagerPort.retrieveEntry("user@test.com")).thenReturn(null);
        when(jwtTokenGeneratorPort.generate(eq("user@test.com"), eq(0), any())).thenReturn("jwt-token");

        LoginDTO result = serviceCase.login(new LoginCommand("code-1"));

        assertThat(result.valid()).isTrue();
        assertThat(result.jwtToken()).isEqualTo("jwt-token");
        verify(jwtTokenCacheManagerPort).overrideEntry("user@test.com", "jwt-token");
    }

    @Test
    void returns_jwt_token_with_incremented_version_when_cached_token_exists() {
        AuthTokenDTO cached = new AuthTokenDTO("user@test.com", "issuer", null, null, 3);
        when(exchangeAuthorizationCodePort.exchangeForIdToken("code-1")).thenReturn(Optional.of("id-token"));
        when(validateIdTokenPort.getUserIfValid("id-token")).thenReturn(Optional.of(new ValidatedIdTokenDTO("user@test.com", "http://img/u.png")));
        when(jwtTokenCacheManagerPort.retrieveEntry("user@test.com")).thenReturn(cached);
        when(jwtTokenGeneratorPort.generate(eq("user@test.com"), eq(4), any())).thenReturn("jwt-token-v4");

        LoginDTO result = serviceCase.login(new LoginCommand("code-1"));

        assertThat(result.jwtToken()).isEqualTo("jwt-token-v4");
        verify(jwtTokenGeneratorPort).generate(eq("user@test.com"), eq(4), any());
    }

    @Test
    void creates_user_when_it_does_not_exist() {
        when(exchangeAuthorizationCodePort.exchangeForIdToken("code-1")).thenReturn(Optional.of("id-token"));
        when(validateIdTokenPort.getUserIfValid("id-token")).thenReturn(Optional.of(new ValidatedIdTokenDTO("user@test.com", "http://img/u.png")));
        when(jwtTokenCacheManagerPort.retrieveEntry("user@test.com")).thenReturn(null);
        when(jwtTokenGeneratorPort.generate(eq("user@test.com"), eq(0), any())).thenReturn("jwt-token");
        when(getUserInfoPersistencePort.findById("user@test.com")).thenReturn(null);

        serviceCase.login(new LoginCommand("code-1"));

        verify(createUserPersistencePort).createUser("user@test.com", "http://img/u.png");
    }

    @Test
    void does_not_create_user_when_it_already_exists() {
        when(exchangeAuthorizationCodePort.exchangeForIdToken("code-1")).thenReturn(Optional.of("id-token"));
        when(validateIdTokenPort.getUserIfValid("id-token")).thenReturn(Optional.of(new ValidatedIdTokenDTO("user@test.com", "http://img/u.png")));
        when(jwtTokenCacheManagerPort.retrieveEntry("user@test.com")).thenReturn(null);
        when(jwtTokenGeneratorPort.generate(eq("user@test.com"), eq(0), any())).thenReturn("jwt-token");
        when(getUserInfoPersistencePort.findById("user@test.com"))
                .thenReturn(new UserInfoDTO("user@test.com", "user@test.com", "http://img/u.png", false));

        serviceCase.login(new LoginCommand("code-1"));

        verify(createUserPersistencePort, never()).createUser(any(), any());
    }
}
