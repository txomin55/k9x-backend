package com.k9x.domain.rankings.results;

import java.math.BigDecimal;

/**
 * One competitor's outcome in one event, as read from that event's classification.
 *
 * <p>{@code team} and {@code country} come from the classification rather than from the dog's current
 * record, so they are the values as of that event.
 */
public record RankingCompetitorResult(
        String dogIdentification,
        String dogName,
        String team,
        String country,
        BigDecimal totalScore,
        boolean notCompeting,
        boolean reserve
) {

    /**
     * A competitor marked as not competing did not really take part, so the ranking treats it as absent.
     */
    public boolean hasScore() {
        return totalScore != null && !notCompeting;
    }
}
