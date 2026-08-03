package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.port.GetDogRankEventResultsPersistencePort;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankEventResultDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.SnapDogRank;
import org.jooq.DSLContext;

import java.util.List;

public class GetDogRankEventResultsJooqAdapter implements GetDogRankEventResultsPersistencePort {

    private final DSLContext dsl;

    public GetDogRankEventResultsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchDogRankEventResultDTO> getEventResults() {
        SnapDogRank dr = Tables.SNAP_DOG_RANK;
        return dsl.select(dr.DOG_ID, dr.DISCIPLINE, dr.EVENT_ID, dr.RANK, dr.APPLYING_TIMESTAMP)
                .from(dr)
                .orderBy(dr.DOG_ID, dr.APPLYING_TIMESTAMP)
                .fetch(r -> new FetchDogRankEventResultDTO(
                        r.get(dr.DOG_ID), r.get(dr.DISCIPLINE), r.get(dr.EVENT_ID),
                        r.get(dr.RANK), r.get(dr.APPLYING_TIMESTAMP)));
    }
}
