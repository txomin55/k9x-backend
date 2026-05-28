package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.port.GetStageDetailPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageDetailDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailEventDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Competitions;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Organizers;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Stages;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.Events;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.List;

public class GetStageDetailJooqAdapter implements GetStageDetailPersistencePort {

    private final DSLContext dsl;

    public GetStageDetailJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public FetchStageDetailDTO getStage(String id) {
        Stages s = Tables.STAGES;
        Competitions c = Tables.COMPETITIONS;
        Organizers o = Tables.ORGANIZERS;
        Events e = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENTS;

        Field<String> eventName = e.NAME.as("event_name");
        Field<String> organizerName = o.NAME.as("organizer_name");

        Result<?> records = dsl.select(
                        s.ID, s.NAME, s.DATE_FROM, s.DATE_TO, s.DELETED_AT,
                        c.ADDRESS,
                        organizerName,
                        e.ID.as("event_id"), eventName, e.CONFIGURATION_ID)
                .from(s)
                .join(c).on(c.ID.eq(s.COMPETITION_ID))
                .join(o).on(o.USER_ID.eq(c.CREATOR))
                .leftJoin(e).on(e.STAGE_ID.eq(s.ID).and(e.DELETED_AT.isNull()))
                .where(s.ID.eq(id))
                .fetch();

        if (records.isEmpty()) {
            return null;
        }

        Record first = records.getFirst();
        List<FetchStageDetailEventDTO> events = new ArrayList<>();

        for (Record r : records) {
            String eventId = r.get(e.ID.as("event_id"));
            if (eventId != null) {
                events.add(new FetchStageDetailEventDTO(
                        eventId, r.get(eventName), r.get(e.CONFIGURATION_ID), null));
            }
        }

        return new FetchStageDetailDTO(
                first.get(s.ID),
                first.get(s.NAME),
                first.get(s.DATE_FROM),
                first.get(s.DATE_TO),
                first.get(c.ADDRESS),
                first.get(organizerName),
                first.get(s.DELETED_AT),
                events);
    }
}
