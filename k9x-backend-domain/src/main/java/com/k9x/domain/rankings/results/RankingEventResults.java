package com.k9x.domain.rankings.results;

import java.util.List;

/** The classification of one event of the ranking, reduced to what the aggregation needs. */
public record RankingEventResults(String eventId, List<RankingCompetitorResult> competitors) {
}
