package com.k9x.infrastructure.out.postgres.events;

import com.k9x.application.events.obdx.use_cases.port.CreateObdxEventPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class CreateEventJooqAdapter implements CreateObdxEventPersistencePort {

    private final DSLContext dsl;

    public CreateEventJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void createEvent(String id, String name, String stageId, String discipline, String creator, long createdAt) {
        dsl.insertInto(Tables.EVENTS)
                .set(Tables.EVENTS.ID, id)
                .set(Tables.EVENTS.NAME, name)
                .set(Tables.EVENTS.STAGE_ID, stageId)
                .set(Tables.EVENTS.DISCIPLINE, discipline)
                .set(Tables.EVENTS.CREATOR, creator)
                .set(Tables.EVENTS.CREATED_AT, createdAt)
                .set(Tables.EVENTS.LAST_UPDATE, createdAt)
                .execute();
    }
}
