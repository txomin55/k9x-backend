package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ObdxCompetitorEventScoreTest {

    // FCI grade 3 worked example: eventScore 800, band floor 601 (span 199), first qualification 192, max 320.
    private static final int EVENT_SCORE = 800;
    private static final int BAND_MIN = 601;
    private static final BigDecimal FIRST_QUAL = new BigDecimal("192");
    private static final BigDecimal MAX = new BigDecimal("320");

    private BigDecimal score(String total) {
        return ObdxCompetitorEventScore.of(EVENT_SCORE, BAND_MIN, FIRST_QUAL, new BigDecimal(total), MAX);
    }

    @Test
    void a_competitor_below_the_first_qualification_drops_to_the_top_of_the_range_below() {
        assertThat(score("191")).isEqualByComparingTo("600");
        assertThat(score("0")).isEqualByComparingTo("600");
    }

    @Test
    void reaching_the_first_qualification_unlocks_ten_percent_of_the_span() {
        // 601 + 0.10·199 = 620.90
        assertThat(score("192")).isEqualByComparingTo("620.90");
    }

    @Test
    void performance_distributes_the_remaining_ninety_percent_by_progress_above_the_first_qualification() {
        // total 256 -> progress (256-192)/(320-192) = 0.5 -> 601 + 19.9 + 0.5·179.1 = 710.45
        assertThat(score("256")).isEqualByComparingTo("710.45");
    }

    @Test
    void a_perfect_score_lands_exactly_on_the_event_score() {
        assertThat(score("320")).isEqualByComparingTo("800.00");
    }

    @Test
    void progress_is_clamped_so_a_total_above_max_never_exceeds_the_event_score() {
        assertThat(score("400")).isEqualByComparingTo("800.00");
    }

    @Test
    void without_qualifications_progress_runs_from_zero_and_there_is_no_drop() {
        // firstQualMin null -> qualThreshold 0: 601 + 19.9 + (256/320)·179.1 = 601 + 19.9 + 143.28 = 764.18
        BigDecimal result = ObdxCompetitorEventScore.of(EVENT_SCORE, BAND_MIN, null, new BigDecimal("256"), MAX);
        assertThat(result).isEqualByComparingTo("764.18");
    }
}
