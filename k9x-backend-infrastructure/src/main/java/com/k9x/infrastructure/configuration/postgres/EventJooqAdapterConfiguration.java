package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.events.obdx.port.*;
import com.k9x.infrastructure.out.postgres.events.obdx.*;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventJooqAdapterConfiguration {

    private final DSLContext dsl;

    EventJooqAdapterConfiguration(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Bean
    public GetObdxEventCollectorPersistencePort getEventCollectorPersistencePort() {
        return new GetObdxEventCollectorJooqAdapter(dsl);
    }
}
