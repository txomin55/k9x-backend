package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.port.ReplaceDogRankingSnapshotPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.SnapDogIndexHistory;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.SnapDogRanking;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

/**
 * Rewrites {@code k9x.snap_dog_ranking} with a {@code DELETE} and an {@code INSERT ... SELECT}, so the whole
 * rebuild runs inside Postgres without loading a row in the heap. {@code DELETE} rather than {@code TRUNCATE}:
 * truncating locks the table against the public reads until the cron's transaction commits, while deleting lets
 * them keep reading the previous snapshot meanwhile. The current index is the latest history record per dog,
 * the same rule as the public directory; deleted dogs are left out.
 */
public class ReplaceDogRankingSnapshotJooqAdapter implements ReplaceDogRankingSnapshotPersistencePort {

    private final DSLContext dsl;

    public ReplaceDogRankingSnapshotJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void replace(long computedAt) {
        SnapDogRanking ranking = Tables.SNAP_DOG_RANKING;
        SnapDogIndexHistory history = Tables.SNAP_DOG_INDEX_HISTORY;

        dsl.deleteFrom(ranking).execute();
        dsl.insertInto(ranking, ranking.DOG_IDENTIFICATION, ranking.RANK, ranking.COUNTRY, ranking.COMPUTED_AT)
                .select(dsl.select(history.DOG_IDENTIFICATION, history.RANK, Tables.DOGS.COUNTRY, DSL.val(computedAt))
                        .distinctOn(history.DOG_IDENTIFICATION)
                        .from(history)
                        .join(Tables.DOGS)
                        .on(Tables.DOGS.IDENTIFICATION.eq(history.DOG_IDENTIFICATION)
                                .and(Tables.DOGS.DELETED_AT.isNull()))
                        .orderBy(history.DOG_IDENTIFICATION, history.APPLYING_TIMESTAMP.desc()))
                .execute();
    }
}
