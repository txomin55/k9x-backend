package com.k9x.configuration.secured.notifications;

import com.k9x.application.notifications.port.GetNotificationListPersistencePort;
import com.k9x.application.notifications.port.MarkNotificationsSeenPersistencePort;
import com.k9x.application.notifications.use_case.GetNotificationListServiceCase;
import com.k9x.application.notifications.use_case.MarkNotificationsSeenServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredNotificationsUseCaseConfiguration {

    @Bean
    public GetNotificationListServiceCase getNotificationListServiceCase(
            GetNotificationListPersistencePort getNotificationListPersistencePort) {
        return new GetNotificationListServiceCase(getNotificationListPersistencePort);
    }

    @Bean
    public MarkNotificationsSeenServiceCase markNotificationsSeenServiceCase(
            MarkNotificationsSeenPersistencePort markNotificationsSeenPersistencePort) {
        return new MarkNotificationsSeenServiceCase(markNotificationsSeenPersistencePort);
    }
}
