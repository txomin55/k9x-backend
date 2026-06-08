package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.events.obdx.port.*;
import com.k9x.application.events.obdx.use_case.port.CreateObdxEventPersistencePort;
import com.k9x.infrastructure.out.postgres.events.CreateEventJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.DeleteEventJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.EnrollEventJooqAdapter;
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
    public CreateObdxEventPersistencePort createEventPersistencePort() {
        return new CreateEventJooqAdapter(dsl);
    }

    @Bean
    public DeleteObdxEventPersistencePort deleteEventPersistencePort() {
        return new DeleteEventJooqAdapter(dsl);
    }

    @Bean
    public UpdateObdxEventPersistencePort updateEventPersistencePort() {
        return new UpdateObdxEventJooqAdapter(dsl);
    }

    @Bean
    public EnrollObdxEventPersistencePort enrollEventPersistencePort() {
        return new EnrollEventJooqAdapter(dsl);
    }

    @Bean
    public GetObdxEventCollectorPersistencePort getEventCollectorPersistencePort() {
        return new GetObdxEventCollectorJooqAdapter(dsl);
    }

    @Bean
    public UpdateObdxScorePersistencePort updateScorePersistencePort() {
        return new UpdateObdxScoreJooqAdapter(dsl);
    }
}
