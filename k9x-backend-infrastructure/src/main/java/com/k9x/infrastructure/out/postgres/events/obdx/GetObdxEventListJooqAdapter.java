package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.port.GetObdxEventListPersistencePort;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Stages;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.Events;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.List;

public class GetObdxEventListJooqAdapter implements GetObdxEventListPersistencePort {

    private final DSLContext dsl;

    public GetObdxEventListJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchObdxEventDTO> getEvents(List<String> stageIds) {
        Events e = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENTS;
        Stages s = Tables.STAGES;

        Field<String> eventId = e.ID.as("event_id");
        Field<String> eventName = e.NAME.as("event_name");
        Field<String> stageId = s.ID.as("stage_id");
        Field<String> stageName = s.NAME.as("stage_name");

        return dsl.select(eventId, eventName, stageId, stageName)
                .from(e)
                .join(s).on(s.ID.eq(e.STAGE_ID))
                .where(e.STAGE_ID.in(stageIds))
                .and(e.DELETED_AT.isNull())
                .fetch(r -> new FetchObdxEventDTO(
                        r.get(eventId),
                        r.get(eventName),
                        r.get(stageId),
                        r.get(stageName)
                ));
    }
}
