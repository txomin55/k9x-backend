package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.port.GetDogRankingDistributionPersistencePort;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankingDistributionDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.SnapDogRanking;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.Map;

/**
 * Counts the ranking snapshot's dogs per index; the bands are the domain's job. The country filter reads the
 * country stamped in the snapshot, not the dog's current one, so a dog that moves meanwhile stays counted where it
 * was ranked. The snapshot's instant is read on its own so it is known even when no dog clears the threshold.
 */
public class GetDogRankingDistributionJooqAdapter implements GetDogRankingDistributionPersistencePort {

    private static final SnapDogRanking RANKING = Tables.SNAP_DOG_RANKING;

    private final DSLContext dsl;

    public GetDogRankingDistributionJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public FetchDogRankingDistributionDTO getDistribution(String country, int minIndex) {
        Field<Integer> dogs = DSL.count();
        Map<Integer, Integer> dogsByIndex = dsl.select(RANKING.RANK, dogs)
                .from(RANKING)
                .where(RANKING.RANK.ge(minIndex))
                .and(country(country))
                .groupBy(RANKING.RANK)
                .fetchMap(RANKING.RANK, dogs);
        Long computedAt = dsl.select(DSL.max(RANKING.COMPUTED_AT)).from(RANKING).fetchOne(0, Long.class);
        return new FetchDogRankingDistributionDTO(computedAt, dogsByIndex);
    }

    private static Condition country(String country) {
        return country == null ? DSL.noCondition() : RANKING.COUNTRY.eq(country);
    }
}
