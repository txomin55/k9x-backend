package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageListDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListEventDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Competitions;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Organizers;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Stages;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.Events;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetStagesJooqAdapter implements GetStageListPersistencePort {

    private final DSLContext dsl;

    public GetStagesJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchStageListDTO> getStages() {
        Stages s = Tables.STAGES;
        Competitions c = Tables.COMPETITIONS;
        Organizers o = Tables.ORGANIZERS;
        Events e = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENTS;
        EventCompetitors ec = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;

        Field<String> eventName = e.NAME.as("event_name");
        Field<String> organizerName = o.NAME.as("organizer_name");
        Field<Integer> competitorCount = dsl.selectCount()
                .from(ec)
                .where(ec.EVENT_ID.eq(e.ID))
                .asField("competitor_count");

        Result<?> records = dsl.select(
                        s.ID, s.NAME, s.DATE_FROM, s.DATE_TO,
                        c.DESCRIPTION, c.COUNTRY, c.ADDRESS, c.COORD_ALT, c.COORD_LONG,
                        organizerName,
                        e.ID.as("event_id"), eventName, e.CONFIGURATION_ID,
                        competitorCount)
                .from(s)
                .join(c).on(c.ID.eq(s.COMPETITION_ID))
                .join(o).on(o.USER_ID.eq(c.CREATOR))
                .join(e).on(e.STAGE_ID.eq(s.ID).and(e.DELETED_AT.isNull()))
                .where(s.DELETED_AT.isNull())
                .orderBy(s.DATE_FROM.asc())
                .fetch();

        Map<String, FetchStageListDTO> stageMap = new LinkedHashMap<>();
        records.forEach(r -> {
            String stageId = r.get(s.ID);
            stageMap.putIfAbsent(stageId, new FetchStageListDTO(
                    stageId, r.get(s.NAME), r.get(c.DESCRIPTION), r.get(c.COUNTRY),
                    r.get(c.ADDRESS), r.get(c.COORD_ALT), r.get(c.COORD_LONG),
                    r.get(s.DATE_FROM), r.get(s.DATE_TO),
                    r.get(organizerName),
                    new ArrayList<>(), null));

            String eventId = r.get(e.ID.as("event_id"));
            if (eventId != null) {
                stageMap.get(stageId).events().add(new FetchStageListEventDTO(
                        eventId, r.get(eventName), r.get(e.CONFIGURATION_ID), null,
                        r.get(competitorCount), null));
            }
        });

        return List.copyOf(stageMap.values());
    }
}
