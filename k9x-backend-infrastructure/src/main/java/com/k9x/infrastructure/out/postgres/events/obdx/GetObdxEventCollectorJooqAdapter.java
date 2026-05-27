package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.port.GetObdxEventCollectorPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
import org.jooq.DSLContext;

public class GetObdxEventCollectorJooqAdapter implements GetObdxEventCollectorPersistencePort {

    private final DSLContext dsl;

    public GetObdxEventCollectorJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public String getCollectorId(String eventId, String judgeId) {
        return dsl.select(Tables.EVENT_JUDGES.COLLECTOR_ID)
                .from(Tables.EVENT_JUDGES)
                .where(Tables.EVENT_JUDGES.EVENT_ID.eq(eventId))
                .and(Tables.EVENT_JUDGES.JUDGE_ID.eq(judgeId))
                .fetchOptional(r -> r.get(Tables.EVENT_JUDGES.COLLECTOR_ID))
                .orElse(null);
    }
}
