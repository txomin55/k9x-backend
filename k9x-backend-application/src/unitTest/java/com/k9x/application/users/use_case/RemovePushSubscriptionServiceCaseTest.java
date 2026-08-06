package com.k9x.application.users.use_case;

import com.k9x.application.users.port.DeletePushSubscriptionPersistencePort;
import com.k9x.application.users.use_case.command.RemovePushSubscriptionCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RemovePushSubscriptionServiceCaseTest {

    @Mock
    DeletePushSubscriptionPersistencePort deletePushSubscriptionPersistencePort;

    private RemovePushSubscriptionServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new RemovePushSubscriptionServiceCase(deletePushSubscriptionPersistencePort);
    }

    @Test
    void removes_subscription_scoped_to_the_authenticated_user() {
        serviceCase.removePushSubscription(new RemovePushSubscriptionCommand("https://fcm/endpoint"), "user@example.com");

        verify(deletePushSubscriptionPersistencePort).deleteByEndpointAndUserId("https://fcm/endpoint", "user@example.com");
    }
}
