package com.k9x.application.rankings.use_case.dto;

/**
 * Row of the ranking list. Carries the event count only: the editor reads the ranking by id when it opens,
 * which already returns the full event set.
 */
public record FetchRankingListItemDTO(
        String id,
        String name,
        int eventCount,
        String groupBy,
        String includeBy,
        Integer includedCount,
        boolean includeReserves
) {
}
