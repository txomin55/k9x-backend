package com.k9x.domain.disciplines.obdx;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ObdxCompetitorEventScoreTest {

    // FCI grade 3 worked example: eventScore 800, band floor 601 (span 199), qualifications B 192 / MB 224 /
    // EXC 256 (the knee), max 320.
    private static final int EVENT_SCORE = 800;
    private static final int BAND_MIN = 601;
    private static final BigDecimal FIRST_QUAL = new BigDecimal("192");
    private static final BigDecimal TOP_QUAL = new BigDecimal("256");
    private static final BigDecimal MAX = new BigDecimal("320");

    private void assertScore(String total, String expected) {
        BigDecimal actual = ObdxCompetitorEventScore.of(EVENT_SCORE, BAND_MIN, FIRST_QUAL, TOP_QUAL,
                new BigDecimal(total), MAX);
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> total + " -> expected " + expected + " but was " + actual);
    }

    @Test
    void a_competitor_below_the_first_qualification_drops_to_the_top_of_the_range_below() {
        assertScore("191", "600");
        assertScore("0", "600");
    }

    @Test
    void reaching_the_first_qualification_unlocks_ten_percent_of_the_span() {
        assertScore("192", "620.90");   // 601 + 0.10·199
    }

    @Test
    void the_knee_share_is_earned_climbing_from_the_first_to_the_top_qualification() {
        assertScore("224", "697.02");   // MB, halfway B->EXC -> 620.9 + 0.5·(0.85·179.1)
        assertScore("256", "773.14");   // EXC (knee) -> 620.9 + 0.85·179.1
    }

    @Test
    void the_polish_share_is_earned_from_the_top_qualification_up_to_the_maximum() {
        assertScore("288", "786.57");   // halfway EXC->max
        assertScore("320", "800.00");   // perfect score lands on the event score
    }

    @Test
    void performance_above_the_maximum_never_exceeds_the_event_score() {
        assertScore("400", "800.00");
    }

    @Test
    void without_a_distinct_top_qualification_the_window_is_a_single_linear_ramp() {
        // Only one qualification (top == first): no knee, window filled linearly from the qualification to max.
        // 256 -> 620.9 + (256-192)/(320-192)·179.1 = 620.9 + 0.5·179.1 = 710.45
        BigDecimal result = ObdxCompetitorEventScore.of(EVENT_SCORE, BAND_MIN, FIRST_QUAL, FIRST_QUAL,
                new BigDecimal("256"), MAX);
        assertEquals(0, new BigDecimal("710.45").compareTo(result), result::toString);
    }

    @Test
    void without_qualifications_there_is_no_drop_and_the_window_ramps_from_zero() {
        // firstQual null, topQual null: no drop; window uses total/max. 100/320 = 0.3125 -> 620.9 + 0.3125·179.1
        BigDecimal result = ObdxCompetitorEventScore.of(EVENT_SCORE, BAND_MIN, null, null, new BigDecimal("100"), MAX);
        assertEquals(0, new BigDecimal("676.87").compareTo(result), result::toString);
    }

    @Test
    void the_drop_carries_the_same_scale_as_every_other_result() {
        BigDecimal drop = ObdxCompetitorEventScore.of(EVENT_SCORE, BAND_MIN, FIRST_QUAL, TOP_QUAL,
                new BigDecimal("191"), MAX);
        assertEquals(2, drop.scale());
    }

    /**
     * The floor is the <em>grade's</em>, not the category sub-band's: a world championship final spans
     * {@code 1000 - 601 = 399}, and failing it drops to the same 600 as failing a club trial.
     */
    @Test
    void a_world_championship_final_spans_from_the_grade_floor_up_to_1000() {
        assertWcFinalScore("191", "600");
        assertWcFinalScore("192", "640.90");    // 601 + 0.10·399
        assertWcFinalScore("224", "793.52");
        assertWcFinalScore("256", "946.14");    // EXC (knee)
        assertWcFinalScore("288", "973.07");
        assertWcFinalScore("320", "1000.00");
    }

    @Test
    void a_disqualified_competitor_earns_nothing_even_with_recorded_scores() {
        assertNull(ObdxCompetitorEventScore.ofEvent(EVENT_SCORE, "OBDX.FCI_GRADE_3.V0", FIRST_QUAL, TOP_QUAL,
                new BigDecimal("300"), MAX, true, true));
    }

    @Test
    void of_event_resolves_the_band_floor_and_guards_the_uncomputable_cases() {
        assertEquals(0, new BigDecimal("773.14").compareTo(
                ObdxCompetitorEventScore.ofEvent(EVENT_SCORE, "OBDX.FCI_GRADE_3.V0", FIRST_QUAL, TOP_QUAL,
                        new BigDecimal("256"), MAX, true, false)));
        assertNull(ObdxCompetitorEventScore.ofEvent(null, "OBDX.FCI_GRADE_3.V0", FIRST_QUAL, TOP_QUAL,
                new BigDecimal("256"), MAX, true, false));
        assertNull(ObdxCompetitorEventScore.ofEvent(EVENT_SCORE, "OBDX.FCI_GRADE_3.V0", FIRST_QUAL, TOP_QUAL,
                new BigDecimal("256"), MAX, false, false));
        assertNull(ObdxCompetitorEventScore.ofEvent(EVENT_SCORE, "UNKNOWN.V0", FIRST_QUAL, TOP_QUAL,
                new BigDecimal("256"), MAX, true, false));
    }

    private void assertWcFinalScore(String total, String expected) {
        BigDecimal actual = ObdxCompetitorEventScore.of(1000, BAND_MIN, FIRST_QUAL, TOP_QUAL,
                new BigDecimal(total), MAX);
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> total + " -> expected " + expected + " but was " + actual);
    }
}
