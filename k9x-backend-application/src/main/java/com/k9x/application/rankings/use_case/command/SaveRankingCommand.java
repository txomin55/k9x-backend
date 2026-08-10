package com.k9x.application.rankings.use_case.command;

import com.k9x.domain.rankings.RankingIncludeBy;
import com.k9x.domain.rankings.RankingGroupBy;

import java.util.List;

/**
 * Inbound mutation command for a ranking write. The criteria arrive already resolved to their domain
 * enums, so an unknown value fails at the REST boundary with a domain error instead of travelling
 * through the stack as a raw string.
 */
public record SaveRankingCommand(
        String rankingId,
        String name,
        List<String> eventIds,
        RankingGroupBy groupBy,
        RankingIncludeBy includeBy,
        Integer includedCount,
        boolean includeReserves
) {
}
