package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.port.DeleteObdxEventPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
import org.jooq.DSLContext;

public class DeleteObdxEventJooqAdapter implements DeleteObdxEventPersistencePort {

    private final DSLContext dsl;

    public DeleteObdxEventJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void deleteEvent(String id, long deletedAt) {
        dsl.update(Tables.EVENTS)
                .set(Tables.EVENTS.DELETED_AT, deletedAt)
                .where(Tables.EVENTS.ID.eq(id))
                .execute();
    }
}
