package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.port.GetCollectionListPersistencePort;
import com.k9x.application.collections.use_case.dto.FetchCollectionDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeDTO;
import com.k9x.domain.shared.UtcDates;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.*;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventJudges;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetCollectionListJooqAdapter implements GetCollectionListPersistencePort {

    private final DSLContext dsl;

    public GetCollectionListJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchCollectionDTO> getCollections(String collectorEmail, long nowMillis) {
        Events e = Tables.EVENTS;
        EventJudges ej = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_JUDGES;
        Stages s = Tables.STAGES;
        Competitions c = Tables.COMPETITIONS;
        Judges j = Tables.JUDGES;
        Users u = Tables.USERS;

        // A stage is visible from the whole UTC day of its date_from until the whole UTC day of its
        // date_to (day-based, matching the detail flow's isAfterUtcDay expiry semantics).
        long dayStart = UtcDates.startOfUtcDay(nowMillis);
        long dayEnd = UtcDates.endOfUtcDay(nowMillis);

        var records = dsl.select(e.ID, e.NAME, e.DISCIPLINE, s.NAME, c.NAME, j.ID, j.NAME)
                .from(e)
                .join(s).on(s.ID.eq(e.STAGE_ID).and(s.DELETED_AT.isNull()))
                .join(c).on(c.ID.eq(s.COMPETITION_ID).and(c.DELETED_AT.isNull()))
                .join(ej).on(ej.EVENT_ID.eq(e.ID))
                .join(j).on(j.ID.eq(ej.JUDGE_ID).and(j.DELETED_AT.isNull()))
                .join(u).on(u.ID.eq(ej.COLLECTOR_ID))
                .where(u.EMAIL.eq(collectorEmail))
                .and(s.DATE_FROM.lessOrEqual(dayEnd))
                .and(s.DATE_TO.greaterOrEqual(dayStart))
                .and(e.DELETED_AT.isNull())
                .fetch();

        Map<String, FetchCollectionDTO> eventMap = new LinkedHashMap<>();
        for (Record r : records) {
            String eventId = r.get(e.ID);
            eventMap.putIfAbsent(eventId, new FetchCollectionDTO(
                    eventId,
                    r.get(e.NAME),
                    r.get(s.NAME),
                    r.get(c.NAME),
                    r.get(e.DISCIPLINE),
                    null,
                    new ArrayList<>()
            ));
            eventMap.get(eventId).judges().add(new FetchCollectionJudgeDTO(
                    r.get(j.ID),
                    r.get(j.NAME)
            ));
        }

        return new ArrayList<>(eventMap.values());
    }
}
