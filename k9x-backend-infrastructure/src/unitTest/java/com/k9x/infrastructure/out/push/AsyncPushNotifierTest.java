package com.k9x.infrastructure.out.push;

import com.k9x.application.notifications.port.SaveNotificationPersistencePort;
import com.k9x.application.notifications.port.SendPushNotificationPort;
import com.k9x.application.notifications.port.payload.SaveNotificationPersistencePayload;
import com.k9x.application.notifications.valueobjects.NotificationType;
import com.k9x.application.notifications.valueobjects.PushDeliveryStatus;
import com.k9x.application.notifications.valueobjects.PushNotification;
import com.k9x.application.users.port.DeletePushSubscriptionPersistencePort;
import com.k9x.application.users.port.GetPushSubscriptionsPersistencePort;
import com.k9x.application.users.use_case.dto.PushSubscriptionTargetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncPushNotifierTest {

    @Mock GetPushSubscriptionsPersistencePort getPushSubscriptionsPersistencePort;
    @Mock DeletePushSubscriptionPersistencePort deletePushSubscriptionPersistencePort;
    @Mock SendPushNotificationPort sendPushNotificationPort;
    @Mock SaveNotificationPersistencePort saveNotificationPersistencePort;
    @Captor ArgumentCaptor<SaveNotificationPersistencePayload> payloadCaptor;

    private AsyncPushNotifier notifier;

    private final PushNotification notification =
            new PushNotification(NotificationType.NEW_ENROLL, Map.of("event_id", "event-1"));

    @BeforeEach
    void setUp() {
        notifier = new AsyncPushNotifier(getPushSubscriptionsPersistencePort,
                deletePushSubscriptionPersistencePort, sendPushNotificationPort, saveNotificationPersistencePort);
    }

    @Test
    void persists_notification_row_when_recipient_has_subscriptions() {
        when(getPushSubscriptionsPersistencePort.getByUserId("creator-1"))
                .thenReturn(List.of(new PushSubscriptionTargetDTO("endpoint-1", "p256dh-1", "auth-1")));
        when(sendPushNotificationPort.send(any(), any())).thenReturn(PushDeliveryStatus.DELIVERED);

        notifier.notify("creator-1", notification);

        verify(saveNotificationPersistencePort, timeout(1000)).save(payloadCaptor.capture());
        SaveNotificationPersistencePayload saved = payloadCaptor.getValue();
        assertThat(saved.userId()).isEqualTo("creator-1");
        assertThat(saved.type()).isEqualTo(NotificationType.NEW_ENROLL);
        assertThat(saved.metadata()).containsEntry("event_id", "event-1");
    }

    @Test
    void does_not_persist_notification_row_when_recipient_has_no_subscriptions() {
        when(getPushSubscriptionsPersistencePort.getByUserId("creator-1")).thenReturn(List.of());

        notifier.notify("creator-1", notification);

        verify(saveNotificationPersistencePort, after(300).never()).save(any());
    }
}
