package com.k9x.infrastructure.configuration.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.notifications.port.GetEventRecipientsPersistencePort;
import com.k9x.application.notifications.port.GetNotificationListPersistencePort;
import com.k9x.application.notifications.port.MarkNotificationsSeenPersistencePort;
import com.k9x.application.notifications.port.SaveEventNotificationPersistencePort;
import com.k9x.application.notifications.port.SaveNotificationPersistencePort;
import com.k9x.infrastructure.out.postgres.notifications.GetEventRecipientsJooqAdapter;
import com.k9x.infrastructure.out.postgres.notifications.GetNotificationListJooqAdapter;
import com.k9x.infrastructure.out.postgres.notifications.MarkNotificationsSeenJooqAdapter;
import com.k9x.infrastructure.out.postgres.notifications.SaveEventNotificationJooqAdapter;
import com.k9x.infrastructure.out.postgres.notifications.SaveNotificationJooqAdapter;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationJooqAdapterConfiguration {

    private final DSLContext dsl;

    NotificationJooqAdapterConfiguration(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Bean
    public SaveNotificationPersistencePort saveNotificationPersistencePort(ObjectMapper objectMapper) {
        return new SaveNotificationJooqAdapter(dsl, objectMapper);
    }

    @Bean
    public GetNotificationListPersistencePort getNotificationListPersistencePort(ObjectMapper objectMapper) {
        return new GetNotificationListJooqAdapter(dsl, objectMapper);
    }

    @Bean
    public MarkNotificationsSeenPersistencePort markNotificationsSeenPersistencePort() {
        return new MarkNotificationsSeenJooqAdapter(dsl);
    }

    @Bean
    public SaveEventNotificationPersistencePort saveEventNotificationPersistencePort() {
        return new SaveEventNotificationJooqAdapter(dsl);
    }

    @Bean
    public GetEventRecipientsPersistencePort getEventRecipientsPersistencePort() {
        return new GetEventRecipientsJooqAdapter(dsl);
    }
}
