package com.k9x.domain.disciplines.obdx;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The ranking score a single competitor earns from an OBDX event. Unlike the raw {@code total/max} scaling it
 * replaces, this rewards <em>merit</em> rather than merely attending a crowded event: the score is bounded by
 * the event's own {@code eventScore} and driven by how far the competitor climbs above the configuration's
 * first qualification threshold.
 *
 * <p>Let {@code configBandMin} be the configuration's rank band floor (e.g. FCI grade 3 → 601), {@code eventScore}
 * the event's rank score and {@code span = eventScore - configBandMin}:
 * <ul>
 *   <li>A competitor that does not even reach the first (lowest) qualification did not earn its place in this
 *       category, so it drops to the top of the range below: {@code configBandMin - 1}.</li>
 *   <li>Reaching the first qualification unlocks a flat 10% of the span.</li>
 *   <li>The remaining 90% of the span is earned by progress from that first qualification up to the maximum
 *       attainable total: {@code progress = (total - firstQualMin) / (max - firstQualMin)} (clamped to
 *       {@code [0, 1]}), so a perfect score lands exactly on {@code eventScore}.</li>
 * </ul>
 * so {@code score = configBandMin + 0.10·span + progress · 0.90·span}.
 */
public final class ObdxCompetitorEventScore {

    private static final BigDecimal QUALIFICATION_UNLOCK_SHARE = new BigDecimal("0.10");
    private static final BigDecimal PERFORMANCE_SHARE = new BigDecimal("0.90");
    private static final int PROGRESS_SCALE = 6;

    private ObdxCompetitorEventScore() {
    }

    /**
     * @param eventScore    the event's own rank score (the ceiling a 100% competitor reaches).
     * @param configBandMin the configuration's rank band floor.
     * @param firstQualMin  the lowest qualification threshold (absolute total score); {@code null} when the
     *                      configuration defines no qualifications, in which case there is no "did not qualify"
     *                      drop and progress runs from 0.
     * @param total         the competitor's achieved weighted total score.
     * @param max           the maximum attainable weighted total score for the event ({@code > 0}).
     */
    public static BigDecimal of(int eventScore, int configBandMin, BigDecimal firstQualMin,
                                BigDecimal total, BigDecimal max) {
        BigDecimal qualThreshold = firstQualMin == null ? BigDecimal.ZERO : firstQualMin;
        if (total.compareTo(qualThreshold) < 0) {
            return BigDecimal.valueOf(configBandMin - 1L);
        }

        BigDecimal span = BigDecimal.valueOf((long) eventScore - configBandMin);
        BigDecimal denominator = max.subtract(qualThreshold);
        BigDecimal progress = denominator.signum() <= 0
                ? BigDecimal.ONE
                : total.subtract(qualThreshold).divide(denominator, PROGRESS_SCALE, RoundingMode.HALF_UP)
                        .min(BigDecimal.ONE).max(BigDecimal.ZERO);

        BigDecimal unlock = span.multiply(QUALIFICATION_UNLOCK_SHARE);
        BigDecimal performance = span.multiply(PERFORMANCE_SHARE).multiply(progress);
        return BigDecimal.valueOf(configBandMin).add(unlock).add(performance).setScale(2, RoundingMode.HALF_UP);
    }
}
