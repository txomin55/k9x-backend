package com.k9x.infrastructure.out.postgres.events;

import com.k9x.application.events.obdx.port.DeleteObdxEventPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class DeleteEventJooqAdapter implements DeleteObdxEventPersistencePort {

    private final DSLContext dsl;

    public DeleteEventJooqAdapter(DSLContext dsl) {
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
