package com.k9x.infrastructure.configuration.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.notifications.port.SaveNotificationPersistencePort;
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
}
