package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.port.GetLatestDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.use_case.dto.FetchLatestDogRankHistoryDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.SnapDogIndexHistory;
import org.jooq.DSLContext;

import java.util.List;

public class GetLatestDogRankHistoryJooqAdapter implements GetLatestDogRankHistoryPersistencePort {

    private final DSLContext dsl;

    public GetLatestDogRankHistoryJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchLatestDogRankHistoryDTO> getLatestHistory() {
        SnapDogIndexHistory h = Tables.SNAP_DOG_INDEX_HISTORY;
        return dsl.select(h.DOG_ID, h.DISCIPLINE, h.RANK, h.APPLYING_TIMESTAMP)
                .distinctOn(h.DOG_ID, h.DISCIPLINE)
                .from(h)
                .orderBy(h.DOG_ID, h.DISCIPLINE, h.APPLYING_TIMESTAMP.desc())
                .fetch(r -> new FetchLatestDogRankHistoryDTO(
                        r.get(h.DOG_ID), r.get(h.DISCIPLINE), r.get(h.RANK), r.get(h.APPLYING_TIMESTAMP)));
    }
}
