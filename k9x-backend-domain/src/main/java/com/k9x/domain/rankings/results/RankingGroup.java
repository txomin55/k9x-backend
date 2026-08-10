package com.k9x.domain.rankings.results;

import java.math.BigDecimal;
import java.util.List;

/**
 * A ranked group: one competitor when grouping individually, otherwise a team or a country.
 */
public record RankingGroup(
        String id,
        String name,
        int position,
        boolean tied,
        BigDecimal total,
        List<RankingMember> members
) {
}
