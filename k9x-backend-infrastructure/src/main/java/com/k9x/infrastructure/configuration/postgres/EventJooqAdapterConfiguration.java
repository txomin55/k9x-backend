package com.k9x.infrastructure.configuration.postgres;

import com.k9x.application.events.obdx.port.CreateObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.DeleteObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.GetClassificationPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxEventCollectorPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxEventListPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.EnrollObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.UpdateObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.UpdateObdxScorePersistencePort;
import com.k9x.infrastructure.out.postgres.events.obdx.CreateObdxEventJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.obdx.DeleteObdxEventJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.obdx.EnrollObdxEventJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.obdx.GetClassificationJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.obdx.GetObdxEventCollectorJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.obdx.GetObdxEventJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.obdx.GetObdxEventListJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.obdx.UpdateObdxEventJooqAdapter;
import com.k9x.infrastructure.out.postgres.events.obdx.UpdateObdxScoreJooqAdapter;
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

    @Bean
    public UpdateObdxEventPersistencePort updateEventPersistencePort() {
        return new UpdateObdxEventJooqAdapter(dsl);
    }

    @Bean
    public EnrollObdxEventPersistencePort enrollEventPersistencePort() {
        return new EnrollObdxEventJooqAdapter(dsl);
    }

    @Bean
    public GetObdxEventCollectorPersistencePort getEventCollectorPersistencePort() {
        return new GetObdxEventCollectorJooqAdapter(dsl);
    }

    @Bean
    public GetObdxEventListPersistencePort getEventListPersistencePort() {
        return new GetObdxEventListJooqAdapter(dsl);
    }

    @Bean
    public UpdateObdxScorePersistencePort updateScorePersistencePort() {
        return new UpdateObdxScoreJooqAdapter(dsl);
    }

    @Bean
    public GetClassificationPersistencePort getClassificationPersistencePort() {
        return new GetClassificationJooqAdapter(dsl);
    }
}
