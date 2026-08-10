package com.k9x.infrastructure.out.postgres.rankings;

import com.k9x.application.rankings.port.GetRankingPersistencePort;
import com.k9x.domain.rankings.RankingIncludeBy;
import com.k9x.domain.rankings.RankingGroupBy;
import com.k9x.domain.rankings.aggregates.Ranking;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GetRankingJooqAdapter implements GetRankingPersistencePort {

    private final DSLContext dsl;

    public GetRankingJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Ranking getRanking(String id) {
        return dsl.select()
                .from(Tables.RANKINGS)
                .where(Tables.RANKINGS.ID.eq(id))
                .fetchOptional(r -> new Ranking(
                        r.get(Tables.RANKINGS.ID),
                        r.get(Tables.RANKINGS.NAME),
                        toList(r.get(Tables.RANKINGS.EVENT_IDS)),
                        RankingGroupBy.from(r.get(Tables.RANKINGS.GROUP_BY)),
                        RankingIncludeBy.from(r.get(Tables.RANKINGS.INCLUDE_BY)),
                        r.get(Tables.RANKINGS.INCLUDED_COUNT),
                        r.get(Tables.RANKINGS.CREATOR),
                        r.get(Tables.RANKINGS.CREATED_AT)
                )).orElse(null);
    }

    private static List<String> toList(String[] ids) {
        return ids == null ? List.of() : Arrays.stream(ids).filter(Objects::nonNull).toList();
    }
}
