package com.k9x.domain.rankings;

import com.k9x.domain.rankings.exceptions.InvalidRankingGroupByException;

import java.util.Arrays;

/**
 * How competitors are grouped within a ranking.
 */
public enum RankingGroupBy {

    INDIVIDUAL,
    TEAM,
    COUNTRY;

    /**
     * Resolves the inbound value, so the {@code valueOf} + try/catch is not repeated at every call site.
     * A missing or unknown value is a domain error, not a deserialization error.
     */
    public static RankingGroupBy from(String value) {
        return Arrays.stream(values())
                .filter(groupBy -> groupBy.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(InvalidRankingGroupByException::new);
    }
}
