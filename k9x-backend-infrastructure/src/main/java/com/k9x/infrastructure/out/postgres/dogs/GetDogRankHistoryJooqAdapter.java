package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.port.GetDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.DogRank;
import org.jooq.DSLContext;

import java.util.List;

public class GetDogRankHistoryJooqAdapter implements GetDogRankHistoryPersistencePort {

    private final DSLContext dsl;

    public GetDogRankHistoryJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchDogRankDTO> getDogRankHistory(String discipline) {
        DogRank dr = Tables.DOG_RANK;
        return dsl.select(dr.DOG_ID, dr.RANK, dr.TIMESTAMP)
                .from(dr)
                .where(dr.DISCIPLINE.eq(discipline))
                .orderBy(dr.DOG_ID, dr.TIMESTAMP)
                .fetch(r -> new FetchDogRankDTO(r.get(dr.DOG_ID), r.get(dr.RANK), r.get(dr.TIMESTAMP)));
    }
}
