package com.k9x.infrastructure.configuration.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.events.snapshot.port.GetEventSnapshotPersistencePort;
import com.k9x.application.events.snapshot.port.GetPendingSnapshotEventsPersistencePort;
import com.k9x.application.events.snapshot.port.SaveEventSnapshotPersistencePort;
import com.k9x.infrastructure.out.postgres.snapshot.GetEventSnapshotJooqAdapter;
import com.k9x.infrastructure.out.postgres.snapshot.GetPendingSnapshotEventsJooqAdapter;
import com.k9x.infrastructure.out.postgres.snapshot.SaveEventSnapshotJooqAdapter;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventSnapshotJooqAdapterConfiguration {

    private final DSLContext dsl;

    EventSnapshotJooqAdapterConfiguration(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Bean
    public GetEventSnapshotPersistencePort getEventSnapshotPersistencePort(ObjectMapper objectMapper) {
        return new GetEventSnapshotJooqAdapter(dsl, objectMapper);
    }

    @Bean
    public SaveEventSnapshotPersistencePort saveEventSnapshotPersistencePort(ObjectMapper objectMapper) {
        return new SaveEventSnapshotJooqAdapter(dsl, objectMapper);
    }

    @Bean
    public GetPendingSnapshotEventsPersistencePort getPendingSnapshotEventsPersistencePort() {
        return new GetPendingSnapshotEventsJooqAdapter(dsl);
    }
}
