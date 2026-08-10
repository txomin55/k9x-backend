package com.k9x.infrastructure.out.postgres.rankings;

import com.k9x.application.rankings.port.GetRankingListPersistencePort;
import com.k9x.application.rankings.use_case.dto.FetchRankingListItemDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.List;

public class GetRankingListJooqAdapter implements GetRankingListPersistencePort {

    /**
     * The events live in an array column, so the count comes from the array itself instead of a join.
     * {@code array_length} is null for an empty array, hence the coalesce.
     */
    private static final Field<Integer> EVENT_COUNT = DSL.field(
            "coalesce(array_length({0}, 1), 0)", Integer.class, Tables.RANKINGS.EVENT_IDS);

    private final DSLContext dsl;

    public GetRankingListJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchRankingListItemDTO> getRankings(String creator) {
        return dsl.select(
                        Tables.RANKINGS.ID,
                        Tables.RANKINGS.NAME,
                        EVENT_COUNT,
                        Tables.RANKINGS.GROUP_BY,
                        Tables.RANKINGS.INCLUDE_BY,
                        Tables.RANKINGS.INCLUDED_COUNT,
                        Tables.RANKINGS.INCLUDE_RESERVES)
                .from(Tables.RANKINGS)
                .where(Tables.RANKINGS.CREATOR.eq(creator))
                .orderBy(Tables.RANKINGS.CREATED_AT.desc())
                .fetch(record -> new FetchRankingListItemDTO(
                        record.get(Tables.RANKINGS.ID),
                        record.get(Tables.RANKINGS.NAME),
                        record.get(EVENT_COUNT),
                        record.get(Tables.RANKINGS.GROUP_BY),
                        record.get(Tables.RANKINGS.INCLUDE_BY),
                        record.get(Tables.RANKINGS.INCLUDED_COUNT),
                        record.get(Tables.RANKINGS.INCLUDE_RESERVES)));
    }
}
