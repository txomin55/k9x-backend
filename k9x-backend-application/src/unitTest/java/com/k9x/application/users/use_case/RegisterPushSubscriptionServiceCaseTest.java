package com.k9x.application.users.use_case;

import com.k9x.application.users.port.RegisterPushSubscriptionPersistencePort;
import com.k9x.application.users.port.payload.RegisterPushSubscriptionPersistencePayload;
import com.k9x.application.users.use_case.command.RegisterPushSubscriptionCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegisterPushSubscriptionServiceCaseTest {

    @Mock
    RegisterPushSubscriptionPersistencePort registerPushSubscriptionPersistencePort;

    @Captor
    ArgumentCaptor<RegisterPushSubscriptionPersistencePayload> payloadCaptor;

    private RegisterPushSubscriptionServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new RegisterPushSubscriptionServiceCase(registerPushSubscriptionPersistencePort);
    }

    @Test
    void registers_subscription_with_command_data_and_authenticated_user() {
        RegisterPushSubscriptionCommand command = new RegisterPushSubscriptionCommand("https://fcm/endpoint", "auth-key", "p256dh-key");

        serviceCase.registerPushSubscription(command, "user@example.com");

        verify(registerPushSubscriptionPersistencePort).registerPushSubscription(payloadCaptor.capture());
        RegisterPushSubscriptionPersistencePayload payload = payloadCaptor.getValue();
        assertThat(payload.endpoint()).isEqualTo("https://fcm/endpoint");
        assertThat(payload.userId()).isEqualTo("user@example.com");
        assertThat(payload.auth()).isEqualTo("auth-key");
        assertThat(payload.p256dh()).isEqualTo("p256dh-key");
        assertThat(payload.lastUpdate()).isPositive();
    }
}
