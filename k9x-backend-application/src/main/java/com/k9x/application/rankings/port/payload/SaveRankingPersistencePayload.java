package com.k9x.application.rankings.port.payload;

import com.k9x.application.rankings.use_case.command.SaveRankingCommand;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.rankings.RankingIncludeBy;
import com.k9x.domain.rankings.RankingGroupBy;

import java.util.List;

public record SaveRankingPersistencePayload(
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

    /**
     * A write replaces the whole row, so the timestamp is re-stamped on every save. The included count
     * is normalised to {@code null} when every result counts, so the column never holds a value that
     * carries no meaning.
     */
    public static SaveRankingPersistencePayload from(SaveRankingCommand command, String creator) {
        return new SaveRankingPersistencePayload(
                command.rankingId(),
                command.name(),
                command.eventIds(),
                command.groupBy(),
                command.includeBy(),
                command.includeBy().includesAll() ? null : command.includedCount(),
                command.includeReserves(),
                creator,
                DateUtils.nowUtcMillis());
    }
}
