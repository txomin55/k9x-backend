package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.port.GetCollectionListPersistencePort;
import com.k9x.application.collections.use_case.dto.FetchCollectionDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Competitions;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Judges;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Stages;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Users;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventJudges;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.Events;
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
        Events e = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENTS;
        EventJudges ej = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_JUDGES;
        Stages s = Tables.STAGES;
        Competitions c = Tables.COMPETITIONS;
        Judges j = Tables.JUDGES;
        Users u = Tables.USERS;

        List<Record> records = dsl.select()
                .from(e)
                .join(s).on(s.ID.eq(e.STAGE_ID).and(s.DELETED_AT.isNull()))
                .join(c).on(c.ID.eq(s.COMPETITION_ID).and(c.DELETED_AT.isNull()))
                .join(ej).on(ej.EVENT_ID.eq(e.ID))
                .join(j).on(j.ID.eq(ej.JUDGE_ID).and(j.DELETED_AT.isNull()))
                .join(u).on(u.ID.eq(ej.COLLECTOR_ID))
                .where(u.EMAIL.eq(collectorEmail))
                .and(s.DATE_TO.greaterOrEqual(nowMillis))
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
