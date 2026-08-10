package com.k9x.domain.rankings.aggregates;

import com.k9x.domain.rankings.RankingIncludeBy;
import com.k9x.domain.rankings.RankingGroupBy;

import java.util.List;

/**
 * A ranking groups N events under a common configuration: how competitors are grouped
 * ({@link RankingGroupBy}) and which of their results are dropped ({@link RankingIncludeBy} plus
 * {@code includedCount}).
 *
 * <p>It is a standalone aggregate: it is deliberately NOT part of the competition aggregate, and the
 * only thing it shares with a competition is a derived identifier. There is no soft delete for
 * rankings, hence no {@code deletedAt}, and no update operation, hence no {@code lastUpdate}: a write
 * replaces the whole row.
 */
public record Ranking(
        String id,
        String name,
        List<String> eventIds,
        RankingGroupBy groupBy,
        RankingIncludeBy includeBy,
        Integer includedCount,
        boolean includeReserves,
        String creator,
        long createdAt
) {
}
