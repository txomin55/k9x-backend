package com.k9x.application.utils.stages;

import com.k9x.domain.shared.UtcDates;

/**
 * Shared "public stages" proximity ordering, reused by the stage list and the competition list.
 * <p>
 * Upcoming/ongoing stages come first ordered by soonest {@code dateFrom} first; past stages come
 * afterwards ordered by most recent {@code dateFrom} first. "Past" means the stage's {@code dateFrom}
 * lies strictly before the reference UTC day.
 */
public final class StageProximity {

    private StageProximity() {}

    /** Compares two stage {@code dateFrom} values by proximity relative to {@code now}. */
    public static int compareByProximity(long aDateFrom, long bDateFrom, long now) {
        boolean aPast = UtcDates.isBeforeUtcDay(aDateFrom, now);
        boolean bPast = UtcDates.isBeforeUtcDay(bDateFrom, now);
        // Upcoming/ongoing stages first; then past stages.
        if (aPast != bPast) {
            return Boolean.compare(aPast, bPast);
        }
        // Upcoming: soonest first (ascending). Past: most recent first (descending).
        return aPast
                ? Long.compare(bDateFrom, aDateFrom)
                : Long.compare(aDateFrom, bDateFrom);
    }
}