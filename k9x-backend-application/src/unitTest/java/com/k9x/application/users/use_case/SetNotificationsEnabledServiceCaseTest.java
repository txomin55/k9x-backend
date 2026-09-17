package com.k9x.application.users.use_case;

import com.k9x.application.users.port.SetUserNotificationsEnabledPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SetNotificationsEnabledServiceCaseTest {

    @Mock
    SetUserNotificationsEnabledPersistencePort setUserNotificationsEnabledPersistencePort;

    private SetNotificationsEnabledServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new SetNotificationsEnabledServiceCase(setUserNotificationsEnabledPersistencePort);
    }

    @Test
    void turns_notifications_off_for_the_whole_account() {
        serviceCase.setNotificationsEnabled("user@example.com", false);

        verify(setUserNotificationsEnabledPersistencePort).setNotificationsEnabled("user@example.com", false);
    }

    @Test
    void turns_notifications_back_on_for_the_whole_account() {
        serviceCase.setNotificationsEnabled("user@example.com", true);

        verify(setUserNotificationsEnabledPersistencePort).setNotificationsEnabled("user@example.com", true);
    }
}
