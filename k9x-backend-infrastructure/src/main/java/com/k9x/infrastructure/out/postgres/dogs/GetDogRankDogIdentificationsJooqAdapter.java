package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.port.GetDogRankDogIdentificationsPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.SnapDogRank;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

public class GetDogRankDogIdentificationsJooqAdapter implements GetDogRankDogIdentificationsPersistencePort {

    private final DSLContext dsl;

    public GetDogRankDogIdentificationsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<String> getDogIdentifications(String after, int limit) {
        SnapDogRank dr = Tables.SNAP_DOG_RANK;
        return dsl.selectDistinct(dr.DOG_IDENTIFICATION)
                .from(dr)
                .where(after == null ? DSL.noCondition() : dr.DOG_IDENTIFICATION.gt(after))
                .orderBy(dr.DOG_IDENTIFICATION)
                .limit(limit)
                .fetch(dr.DOG_IDENTIFICATION);
    }
}
