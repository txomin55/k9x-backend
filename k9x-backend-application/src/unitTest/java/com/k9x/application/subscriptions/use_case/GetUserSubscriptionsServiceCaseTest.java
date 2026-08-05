package com.k9x.application.subscriptions.use_case;

import com.k9x.application.subscriptions.port.GetUserSubscriptionsPersistencePort;
import com.k9x.application.subscriptions.use_case.dto.UserSubscriptionsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserSubscriptionsServiceCaseTest {

    @Mock
    private GetUserSubscriptionsPersistencePort getUserSubscriptionsPersistencePort;

    private GetUserSubscriptionsServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetUserSubscriptionsServiceCase(getUserSubscriptionsPersistencePort);
    }

    @Test
    void returns_the_subscriptions_of_the_requesting_user() {
        when(getUserSubscriptionsPersistencePort.getUserSubscriptions("user@test.com"))
                .thenReturn(new UserSubscriptionsDTO(List.of("event-1", "event-2")));

        UserSubscriptionsDTO result = serviceCase.getUserSubscriptions("user@test.com");

        assertThat(result.eventIds()).containsExactly("event-1", "event-2");
    }
}
