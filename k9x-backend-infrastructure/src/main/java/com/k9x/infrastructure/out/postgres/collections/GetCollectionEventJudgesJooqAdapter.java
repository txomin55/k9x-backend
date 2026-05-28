package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.port.GetCollectionEventJudgesPersistencePort;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Judges;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Users;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventJudges;
import org.jooq.DSLContext;

import java.util.List;

public class GetCollectionEventJudgesJooqAdapter implements GetCollectionEventJudgesPersistencePort {

    private final DSLContext dsl;

    public GetCollectionEventJudgesJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchCollectionJudgeWithCollectorDTO> getJudges(String eventId) {
        EventJudges ej = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_JUDGES;
        Judges j = Tables.JUDGES;
        Users u = Tables.USERS;

        return dsl.select(ej.JUDGE_ID, j.NAME, u.EMAIL)
                .from(ej)
                .join(j).on(j.ID.eq(ej.JUDGE_ID).and(j.DELETED_AT.isNull()))
                .join(u).on(u.ID.eq(ej.COLLECTOR_ID))
                .where(ej.EVENT_ID.eq(eventId))
                .fetch(r -> new FetchCollectionJudgeWithCollectorDTO(
                        r.get(ej.JUDGE_ID),
                        r.get(j.NAME),
                        r.get(u.EMAIL)
                ));
    }
}
