package com.k9x.domain.rankings.results;

import java.util.List;

/** A participant of a group: one row of the expanded matrix, with one cell per event. */
public record RankingMember(String id, String name, List<RankingCell> cells) {
}
