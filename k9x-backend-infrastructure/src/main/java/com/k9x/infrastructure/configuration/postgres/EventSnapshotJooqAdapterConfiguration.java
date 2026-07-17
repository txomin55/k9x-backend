package com.k9x.infrastructure.configuration.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.events.snapshot.port.GetObdxEventSnapshotPersistencePort;
import com.k9x.application.events.snapshot.port.GetPendingSnapshotEventsPersistencePort;
import com.k9x.application.events.snapshot.port.SaveObdxSnapshotPersistencePort;
import com.k9x.infrastructure.out.postgres.events.GetPendingSnapshotEventsJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.obdx.GetObdxEventSnapshotJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.obdx.SaveObdxSnapshotJooqAdapter;
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
    public GetObdxEventSnapshotPersistencePort getObdxEventSnapshotPersistencePort(ObjectMapper objectMapper) {
        return new GetObdxEventSnapshotJooqAdapter(dsl, objectMapper);
    }

    @Bean
    public SaveObdxSnapshotPersistencePort saveObdxSnapshotPersistencePort(ObjectMapper objectMapper) {
        return new SaveObdxSnapshotJooqAdapter(dsl, objectMapper);
    }

    @Bean
    public GetPendingSnapshotEventsPersistencePort getPendingSnapshotEventsPersistencePort() {
        return new GetPendingSnapshotEventsJooqAdapter(dsl);
    }
}
