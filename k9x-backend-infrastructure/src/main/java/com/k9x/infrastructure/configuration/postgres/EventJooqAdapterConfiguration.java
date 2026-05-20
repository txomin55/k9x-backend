package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.events.obdx.port.CreateObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.DeleteObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
import com.k9x.infrastructure.out.postgres.events.obdx.CreateObdxEventJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.obdx.DeleteObdxEventJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.obdx.GetObdxEventJooqAdapter;
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
    public CreateObdxEventPersistencePort createEventPersistencePort() {
        return new CreateObdxEventJooqAdapter(dsl);
    }

    @Bean
    public GetObdxEventPersistencePort getEventPersistencePort() {
        return new GetObdxEventJooqAdapter(dsl);
    }

    @Bean
    public DeleteObdxEventPersistencePort deleteEventPersistencePort() {
        return new DeleteObdxEventJooqAdapter(dsl);
    }
}
