package com.k9x.application.rankings.use_case.dto;

import java.util.List;

/**
 * Read model of a ranking. The criteria travel as strings (the enum {@code name()}), following the same
 * convention as the computed status fields elsewhere.
 */
public record FetchRankingDTO(
        String id,
        String name,
        List<FetchRankingEventDTO> events,
        String groupBy,
        String includeBy,
        Integer includedCount
) {
}
