package com.k9x.domain.dogs.rank;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DogRankIndexTest {

    private static final long MILLIS_PER_MONTH = (long) (30.4375 * 86_400_000.0);
    private static final long NOW = 1_800_000_000_000L;

    private DogRankIndex.Result result(String score, int monthsAgo) {
        return new DogRankIndex.Result(new BigDecimal(score), NOW - monthsAgo * MILLIS_PER_MONTH);
    }

    @Test
    void each_curve_holds_full_weight_during_its_own_plateau() {
        // The plateaus differ on purpose: the level one covers the time it takes to fill the 3 slots, the
        // freshness one only the normal gap between trials.
        assertEquals(1.0, DogRankIndex.levelWeight(0));
        assertEquals(1.0, DogRankIndex.levelWeight(8));
        assertTrue(DogRankIndex.levelWeight(8.5) < 1.0);
        assertEquals(1.0, DogRankIndex.freshness(0));
        assertEquals(1.0, DogRankIndex.freshness(6));
        assertTrue(DogRankIndex.freshness(6.5) < 1.0);
    }

    @Test
    void level_curve_matches_the_spec_anchors() {
        assertEquals(0.85, DogRankIndex.levelWeight(14), 1e-9);
        assertEquals(0.65, DogRankIndex.levelWeight(20), 1e-9);
        assertEquals(0.45, DogRankIndex.levelWeight(26), 1e-9);
        assertEquals(0.25, DogRankIndex.levelWeight(32), 1e-9);
        assertEquals(0.05, DogRankIndex.levelWeight(44), 1e-9);
        assertEquals(0.01, DogRankIndex.levelWeight(56), 1e-9);
        assertEquals(0.01, DogRankIndex.levelWeight(100), 1e-9);
    }

    @Test
    void freshness_curve_falls_steeply_so_a_layoff_bites() {
        assertEquals(0.80, DogRankIndex.freshness(10), 1e-9);
        assertEquals(0.60, DogRankIndex.freshness(16), 1e-9);
        assertEquals(0.40, DogRankIndex.freshness(22), 1e-9);
        assertEquals(0.12, DogRankIndex.freshness(34), 1e-9);
        assertEquals(0.01, DogRankIndex.freshness(58), 1e-9);
        assertEquals(0.01, DogRankIndex.freshness(100), 1e-9);
    }

    @Test
    void the_index_ramps_up_as_the_slots_fill() {
        // Empty slots are worth the prior (201): a single 750 is (750 + 201 + 201) / 3, and only three results
        // put the dog at its real level.
        assertEquals(384, DogRankIndex.of(List.of(result("750", 0)), NOW));
        assertEquals(567, DogRankIndex.of(List.of(result("750", 0), result("750", 1)), NOW));
        assertEquals(750, DogRankIndex.of(List.of(result("750", 0), result("750", 1), result("750", 2)), NOW));
    }

    @Test
    void one_exceptional_result_does_not_define_the_dog() {
        // A debut world final of 950 is worth a third of the level, not the whole of it.
        assertEquals(451, DogRankIndex.of(List.of(result("950", 0)), NOW));
    }

    @Test
    void competing_never_lowers_the_index() {
        int before = DogRankIndex.of(List.of(result("800", 0)), NOW);
        int after = DogRankIndex.of(List.of(result("800", 0), result("200", 0)), NOW);
        assertEquals(401, before);
        assertTrue(after >= before, "a poor result must not displace a better one");
    }

    @Test
    void the_prior_never_inflates_a_dog_above_its_own_best_result() {
        // min(prior, own best) — a dog whose only result is below the prior reads exactly that result.
        assertEquals(200, DogRankIndex.of(List.of(result("200", 0)), NOW));
        assertEquals(150, DogRankIndex.of(List.of(result("150", 0)), NOW));
    }

    @Test
    void a_bad_day_is_diluted_by_the_rest_of_the_history() {
        // 820 (6 months), 810 (3 months), 650 (now): all inside the level plateau -> plain mean 760.
        assertEquals(760, DogRankIndex.of(List.of(result("820", 6), result("810", 3), result("650", 0)), NOW));
    }

    @Test
    void older_results_gradually_lose_weight_in_the_level() {
        // 800 at 20 months (weight 0.65 -> 520) + a fresh 600, third slot filled by the prior:
        // (600 + 520 + 201) / 3 = 440.33.
        assertEquals(440, DogRankIndex.of(List.of(result("800", 20), result("600", 0)), NOW));
    }

    @Test
    void a_fresh_result_outranks_a_degraded_better_one() {
        // A 950 from 20 months ago contributes 617.5, less than a fresh 750, so it loses its slot.
        assertEquals(750, DogRankIndex.of(List.of(result("950", 20),
                result("750", 0), result("750", 1), result("750", 2)), NOW));
    }

    @Test
    void inactivity_degrades_the_index_through_freshness() {
        // Single result 34 months old: level 8 (weight 0.12 on both the result and its prior-capped slots),
        // freshness 0.12 -> the dog is all but out of the ranking.
        assertEquals(18, DogRankIndex.of(List.of(result("700", 34)), NOW));
    }

    @Test
    void freshness_is_driven_by_the_most_recent_result_only() {
        // An old 800 (34 months) plus a fresh 800: freshness snaps back to 1.0.
        assertEquals(401, DogRankIndex.of(List.of(result("800", 34), result("800", 0)), NOW));
    }

    @Test
    void no_dog_ever_disappears_thanks_to_the_floor() {
        assertEquals(1, DogRankIndex.of(List.of(result("700", 120)), NOW));
    }
}
