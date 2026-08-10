package com.k9x.infrastructure.out.postgres.rankings;

import com.k9x.application.rankings.port.SaveRankingPersistencePort;
import com.k9x.application.rankings.port.payload.SaveRankingPersistencePayload;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class SaveRankingJooqAdapter implements SaveRankingPersistencePort {

    private final DSLContext dsl;

    public SaveRankingJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void saveRanking(SaveRankingPersistencePayload payload) {
        dsl.insertInto(Tables.RANKINGS)
                .set(Tables.RANKINGS.ID, payload.id())
                .set(Tables.RANKINGS.NAME, payload.name())
                .set(Tables.RANKINGS.EVENT_IDS, payload.eventIds().toArray(String[]::new))
                .set(Tables.RANKINGS.GROUP_BY, payload.groupBy().name())
                .set(Tables.RANKINGS.INCLUDE_BY, payload.includeBy().name())
                .set(Tables.RANKINGS.INCLUDED_COUNT, payload.includedCount())
                .set(Tables.RANKINGS.CREATOR, payload.creator())
                .set(Tables.RANKINGS.CREATED_AT, payload.createdAt())
                .execute();
    }
}
