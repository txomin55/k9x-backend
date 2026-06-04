package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.port.GetStageDetailPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageDetailCompetitorDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailEventDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.*;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
        Events e = Tables.EVENTS;
        EventCompetitors ec = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;
        Dogs d = Tables.DOGS;

        Field<String> eventName = e.NAME.as("event_name");
        Field<String> organizerName = o.NAME.as("organizer_name");
        Field<String> dogName = d.NAME.as("dog_name");

        Result<?> records = dsl.select(
                        s.ID, s.NAME, s.DATE_FROM, s.DATE_TO, s.DELETED_AT,
                        c.ADDRESS,
                        organizerName,
                        e.ID.as("event_id"), eventName, e.DISCIPLINE, e.CONFIGURATION_ID,
                        d.ID, dogName, d.OWNER, d.COUNTRY, d.TEAM, d.BREED)
                .from(s)
                .join(c).on(c.ID.eq(s.COMPETITION_ID))
                .join(o).on(o.USER_ID.eq(c.CREATOR))
                .leftJoin(e).on(e.STAGE_ID.eq(s.ID).and(e.DELETED_AT.isNull()))
                .leftJoin(ec).on(ec.EVENT_ID.eq(e.ID))
                .leftJoin(d).on(d.ID.eq(ec.DOG_ID).and(d.DELETED_AT.isNull()))
                .where(s.ID.eq(id))
                .fetch();

        if (records.isEmpty()) {
            return null;
        }

        Record first = records.getFirst();
        LinkedHashMap<String, FetchStageDetailEventDTO> events = new LinkedHashMap<>();

        for (Record r : records) {
            String eventId = r.get(e.ID.as("event_id"));
            if (eventId == null) {
                continue;
            }
            events.putIfAbsent(eventId, new FetchStageDetailEventDTO(
                    eventId, r.get(eventName), r.get(e.DISCIPLINE), r.get(e.CONFIGURATION_ID),
                    null, new ArrayList<>()));
            String dogId = r.get(d.ID);
            if (dogId != null) {
                events.get(eventId).competitors().add(new FetchStageDetailCompetitorDTO(
                        dogId, r.get(dogName), r.get(d.OWNER), r.get(d.COUNTRY),
                        r.get(d.TEAM), r.get(d.BREED)));
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
                new ArrayList<>(events.values()));
    }
}
