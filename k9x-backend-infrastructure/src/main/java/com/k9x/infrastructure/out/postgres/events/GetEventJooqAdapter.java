package com.k9x.infrastructure.out.postgres.events;

import com.k9x.application.events.obdx.use_case.port.GetEventPersistencePort;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import org.jooq.DSLContext;

import static com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.EVENTS;

public class GetEventJooqAdapter implements GetEventPersistencePort {

    private final DSLContext dsl;

    public GetEventJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Event getEvent(String id) {
        return dsl.select()
                .from(EVENTS)
                .where(EVENTS.ID.eq(id))
                .fetchOptional(r -> new Event(
                        r.get(EVENTS.ID),
                        r.get(EVENTS.CONFIGURATION_ID),
                        r.get(EVENTS.DISCIPLINE),
                        r.get(EVENTS.NAME),
                        r.get(EVENTS.STAGE_ID),
                        r.get(EVENTS.CREATOR),
                        r.get(EVENTS.LAST_UPDATE),
                        r.get(EVENTS.CREATED_AT),
                        r.get(EVENTS.DELETED_AT),
                        ObdxAvgMethod.valueOf(r.get(EVENTS.SCORE_CALCULATION))
                )).orElse(null);
    }
}
