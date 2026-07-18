package com.k9x.domain.disciplines.obdx;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The ranking score a single competitor earns from an OBDX event. Unlike a raw {@code total/max} scaling it
 * rewards <em>merit</em> rather than merely attending a crowded event: the score is bounded by the event's own
 * {@code eventScore} and driven by how far the competitor climbs through the configuration's qualification
 * tiers.
 *
 * <p>Let {@code configBandMin} be the configuration's rank band floor (e.g. FCI grade 3 → 601), {@code eventScore}
 * the event's rank score and {@code span = eventScore - configBandMin}:
 * <ul>
 *   <li>A competitor that does not even reach the first (lowest) qualification did not earn its place in this
 *       category, so it drops to the top of the range below: {@code configBandMin - 1}.</li>
 *   <li>Reaching the first qualification unlocks a flat 10% of the span.</li>
 *   <li>The remaining 90% window has a <em>knee at the highest qualification</em> the configuration defines
 *       (e.g. EXC): climbing from the first qualification up to that top qualification earns
 *       {@link #KNEE_SHARE 85%} of the window, and the last stretch from the top qualification to the maximum
 *       attainable total earns the remaining 15%. Reaching the top qualification therefore already lands near
 *       the ceiling ("excellence"), and a perfect score lands exactly on {@code eventScore}.</li>
 * </ul>
 */
public final class ObdxCompetitorEventScore {

    private static final BigDecimal QUALIFICATION_UNLOCK_SHARE = new BigDecimal("0.10");
    private static final BigDecimal WINDOW_SHARE = new BigDecimal("0.90");
    /** Share of the 90% window earned by climbing from the first to the highest qualification (the rest is the polish above it). */
    private static final BigDecimal KNEE_SHARE = new BigDecimal("0.85");
    private static final int PROGRESS_SCALE = 6;

    private ObdxCompetitorEventScore() {
    }

    /**
     * @param eventScore    the event's own rank score (the ceiling a 100% competitor reaches).
     * @param configBandMin the configuration's rank band floor.
     * @param firstQualMin  the lowest qualification threshold (absolute total score); {@code null} when the
     *                      configuration defines no qualifications, in which case there is no "did not qualify"
     *                      drop.
     * @param topQualMin    the highest qualification threshold (the knee); {@code null} or equal to
     *                      {@code firstQualMin} degrades to a single linear window with no knee.
     * @param total         the competitor's achieved weighted total score.
     * @param max           the maximum attainable weighted total score for the event ({@code > 0}).
     */
    public static BigDecimal of(int eventScore, int configBandMin, BigDecimal firstQualMin, BigDecimal topQualMin,
                                BigDecimal total, BigDecimal max) {
        BigDecimal firstQual = firstQualMin == null ? BigDecimal.ZERO : firstQualMin;
        if (total.compareTo(firstQual) < 0) {
            return BigDecimal.valueOf(configBandMin - 1L);
        }

        BigDecimal span = BigDecimal.valueOf((long) eventScore - configBandMin);
        BigDecimal window = span.multiply(WINDOW_SHARE);
        BigDecimal base = BigDecimal.valueOf(configBandMin).add(span.multiply(QUALIFICATION_UNLOCK_SHARE));

        BigDecimal result;
        if (topQualMin != null && topQualMin.compareTo(firstQual) > 0) {
            BigDecimal kneeWindow = window.multiply(KNEE_SHARE);
            if (total.compareTo(topQualMin) <= 0) {
                BigDecimal progress = ratio(total.subtract(firstQual), topQualMin.subtract(firstQual));
                result = base.add(progress.multiply(kneeWindow));
            } else {
                BigDecimal polishWindow = window.subtract(kneeWindow);
                BigDecimal progress = ratio(total.subtract(topQualMin), max.subtract(topQualMin));
                result = base.add(kneeWindow).add(progress.multiply(polishWindow));
            }
        } else {
            BigDecimal progress = ratio(total.subtract(firstQual), max.subtract(firstQual));
            result = base.add(progress.multiply(window));
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    /** {@code numerator / denominator} clamped to {@code [0, 1]}; {@code 1} when the denominator is non-positive. */
    private static BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator.signum() <= 0) {
            return BigDecimal.ONE;
        }
        return numerator.divide(denominator, PROGRESS_SCALE, RoundingMode.HALF_UP)
                .min(BigDecimal.ONE).max(BigDecimal.ZERO);
    }
}
