package com.k9x.configuration.notifications;

import com.k9x.application.notifications.port.PushNotifier;
import com.k9x.application.notifications.port.SaveNotificationPersistencePort;
import com.k9x.application.notifications.port.SendPushNotificationPort;
import com.k9x.application.users.port.DeletePushSubscriptionPersistencePort;
import com.k9x.application.users.port.GetPushSubscriptionsPersistencePort;
import com.k9x.infrastructure.out.push.AsyncPushNotifier;
import com.k9x.infrastructure.out.push.NoOpPushNotificationAdapter;
import com.k9x.infrastructure.out.push.WebPushNotificationAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;

@Configuration
public class NotificationConfiguration {

    /**
     * Uses real Web Push delivery only when a VAPID key pair is configured; otherwise falls back to a
     * no-op so the application still boots (and enrollments still work) in environments without keys.
     */
    @Bean
    public SendPushNotificationPort sendPushNotificationPort(
            @Value("${k9x-backend.push.vapid.public-key}") String publicKey,
            @Value("${k9x-backend.push.vapid.private-key}") String privateKey,
            @Value("${k9x-backend.push.vapid.subject}") String subject
    ) throws GeneralSecurityException {
        if (publicKey.isBlank() || privateKey.isBlank()) {
            return new NoOpPushNotificationAdapter();
        }
        return new WebPushNotificationAdapter(publicKey, privateKey, subject);
    }

    @Bean
    public PushNotifier pushNotifier(
            GetPushSubscriptionsPersistencePort getPushSubscriptionsPersistencePort,
            DeletePushSubscriptionPersistencePort deletePushSubscriptionPersistencePort,
            SendPushNotificationPort sendPushNotificationPort,
            SaveNotificationPersistencePort saveNotificationPersistencePort
    ) {
        return new AsyncPushNotifier(
                getPushSubscriptionsPersistencePort, deletePushSubscriptionPersistencePort,
                sendPushNotificationPort, saveNotificationPersistencePort);
    }
}
