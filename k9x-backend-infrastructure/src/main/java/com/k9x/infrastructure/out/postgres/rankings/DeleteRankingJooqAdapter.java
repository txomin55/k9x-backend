package com.k9x.infrastructure.out.postgres.rankings;

import com.k9x.application.rankings.port.DeleteRankingPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

/**
 * Rankings are deleted for real: the table has no {@code deleted_at} column.
 */
public class DeleteRankingJooqAdapter implements DeleteRankingPersistencePort {

    private final DSLContext dsl;

    public DeleteRankingJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void deleteRanking(String id) {
        dsl.deleteFrom(Tables.RANKINGS)
                .where(Tables.RANKINGS.ID.eq(id))
                .execute();
    }
}
