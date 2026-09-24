package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.port.GetDogRankingEntryPersistencePort;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankingEntryDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.SnapDogRanking;
import org.jooq.DSLContext;

import java.util.Optional;

/** Reads a dog's row of the ranking snapshot, with the name of the dog as long as it is still active. */
public class GetDogRankingEntryJooqAdapter implements GetDogRankingEntryPersistencePort {

    private static final SnapDogRanking RANKING = Tables.SNAP_DOG_RANKING;

    private final DSLContext dsl;

    public GetDogRankingEntryJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<FetchDogRankingEntryDTO> getEntry(String dogIdentification) {
        return dsl.select(RANKING.DOG_IDENTIFICATION, Tables.DOGS.NAME, RANKING.RANK, RANKING.COUNTRY)
                .from(RANKING)
                .join(Tables.DOGS)
                .on(Tables.DOGS.IDENTIFICATION.eq(RANKING.DOG_IDENTIFICATION))
                .where(RANKING.DOG_IDENTIFICATION.eq(dogIdentification))
                .and(Tables.DOGS.DELETED_AT.isNull())
                .fetchOptional(r -> new FetchDogRankingEntryDTO(r.get(RANKING.DOG_IDENTIFICATION),
                        r.get(Tables.DOGS.NAME), r.get(RANKING.RANK), r.get(RANKING.COUNTRY)));
    }
}
