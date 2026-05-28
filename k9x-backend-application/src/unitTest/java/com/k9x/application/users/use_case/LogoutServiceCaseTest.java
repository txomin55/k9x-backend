package com.k9x.application.users.use_case;

import com.k9x.application.users.port.JwtTokenCacheManagerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutServiceCaseTest {

    @Mock
    private JwtTokenCacheManagerPort jwtTokenCacheManagerPort;

    private LogoutServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new LogoutServiceCase(jwtTokenCacheManagerPort);
    }

    @Test
    void deletes_cache_entry_for_user_email() {
        serviceCase.logout("user@test.com");

        verify(jwtTokenCacheManagerPort).deleteEntry("user@test.com");
    }
}
