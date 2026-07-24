package com.k9x.application.notifications.use_case;

import com.k9x.application.notifications.port.GetNotificationListPersistencePort;
import com.k9x.application.notifications.use_case.dto.NotificationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNotificationListServiceCaseTest {

    @Mock GetNotificationListPersistencePort getNotificationListPersistencePort;
    private GetNotificationListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetNotificationListServiceCase(getNotificationListPersistencePort);
    }

    @Test
    void returns_only_the_requesting_users_notifications() {
        List<NotificationDTO> stored = List.of(
                new NotificationDTO("1", 1700000000000L, "{\"event_id\":\"event-1\"}", false));
        when(getNotificationListPersistencePort.getByUserId("creator-1")).thenReturn(stored);

        List<NotificationDTO> result = serviceCase.getNotifications("creator-1");

        assertThat(result).isEqualTo(stored);
        verify(getNotificationListPersistencePort).getByUserId("creator-1");
    }
}
