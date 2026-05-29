package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.obdx.ObdxEvent;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
import org.jooq.DSLContext;

public class GetObdxEventJooqAdapter implements GetObdxEventPersistencePort {

    private final DSLContext dsl;

    public GetObdxEventJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public ObdxEvent getEvent(String id) {
        return dsl.select()
                .from(Tables.EVENTS)
                .where(Tables.EVENTS.ID.eq(id))
                .fetchOptional(r -> new ObdxEvent(
                        r.get(Tables.EVENTS.ID),
                        r.get(Tables.EVENTS.CONFIGURATION_ID),
                        r.get(Tables.EVENTS.NAME),
                        r.get(Tables.EVENTS.STAGE_ID),
                        r.get(Tables.EVENTS.CREATOR),
                        r.get(Tables.EVENTS.LAST_UPDATE),
                        r.get(Tables.EVENTS.CREATED_AT),
                        r.get(Tables.EVENTS.DELETED_AT),
                        ObdxAvgMethod.valueOf(r.get(Tables.EVENTS.SCORE_CALCULATION))
                )).orElse(null);
    }
}
