package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.port.EnrollObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.payload.EnrollObdxEventPersistencePayload;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
import org.jooq.DSLContext;

public class EnrollObdxEventJooqAdapter implements EnrollObdxEventPersistencePort {

    private final DSLContext dsl;

    public EnrollObdxEventJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void enrollEvent(String eventId, EnrollObdxEventPersistencePayload payload) {
        dsl.insertInto(Tables.EVENT_COMPETITORS)
                .set(Tables.EVENT_COMPETITORS.EVENT_ID, eventId)
                .set(Tables.EVENT_COMPETITORS.DOG_ID, payload.dogId())
                .set(Tables.EVENT_COMPETITORS.VERIFIED, false)
                .set(Tables.EVENT_COMPETITORS.LAST_UPDATE, payload.lastUpdate())
                .execute();
    }
}
