package com.k9x.domain.rankings.results;

import java.math.BigDecimal;

/**
 * A competitor's cell for one event of the ranking.
 *
 * @param score  the score obtained, or {@code null} when the competitor did not compete in that event
 * @param counts whether the score was added to the group total: false with no score, and false when the
 *               inclusion criterion left it out
 */
public record RankingCell(String eventId, BigDecimal score, boolean counts) {
}
