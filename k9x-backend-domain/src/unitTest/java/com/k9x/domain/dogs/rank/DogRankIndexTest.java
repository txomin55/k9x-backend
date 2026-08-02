package com.k9x.domain.dogs.rank;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DogRankIndexTest {

    private static final long MILLIS_PER_MONTH = (long) (30.4375 * 86_400_000.0);
    private static final long NOW = 1_800_000_000_000L;

    private DogRankIndex.Result result(String score, int monthsAgo) {
        return new DogRankIndex.Result(new BigDecimal(score), NOW - monthsAgo * MILLIS_PER_MONTH);
    }

    @Test
    void both_curves_hold_full_weight_during_the_ten_month_plateau() {
        assertEquals(1.0, DogRankIndex.levelWeight(0));
        assertEquals(1.0, DogRankIndex.levelWeight(10));
        assertEquals(1.0, DogRankIndex.freshness(0));
        assertEquals(1.0, DogRankIndex.freshness(10));
    }

    @Test
    void level_curve_matches_the_spec_anchors() {
        assertEquals(0.75, DogRankIndex.levelWeight(22), 1e-9);
        assertEquals(0.50, DogRankIndex.levelWeight(34), 1e-9);
        assertEquals(0.25, DogRankIndex.levelWeight(46), 1e-9);
        assertEquals(0.10, DogRankIndex.levelWeight(58), 1e-9);
        assertEquals(0.01, DogRankIndex.levelWeight(70), 1e-9);
        assertEquals(0.01, DogRankIndex.levelWeight(100), 1e-9);
    }

    @Test
    void freshness_curve_falls_steeper_out_of_the_plateau_then_rejoins_the_level_curve() {
        assertEquals(0.92, DogRankIndex.freshness(12), 1e-9);
        assertEquals(0.84, DogRankIndex.freshness(18), 1e-9);
        assertEquals(0.75, DogRankIndex.freshness(22), 1e-9);
        assertEquals(0.50, DogRankIndex.freshness(34), 1e-9);
        assertEquals(0.01, DogRankIndex.freshness(100), 1e-9);
    }

    @Test
    void a_single_fresh_result_is_the_index_itself() {
        assertEquals(773, DogRankIndex.of(List.of(result("773.14", 2)), NOW));
    }

    @Test
    void a_bad_day_is_diluted_by_the_rest_of_the_history() {
        // 820 (6 months), 810 (3 months), 650 (now): all in the plateau -> plain mean 760, freshness 1.0.
        int index = DogRankIndex.of(List.of(result("820", 6), result("810", 3), result("650", 0)), NOW);
        assertEquals(760, index);
    }

    @Test
    void older_results_gradually_lose_weight_in_the_level() {
        // 800 at 22 months (weight 0.75) vs 600 now: (800·0.75 + 600·1) / 1.75 = 685.71 -> 686.
        int index = DogRankIndex.of(List.of(result("800", 22), result("600", 0)), NOW);
        assertEquals(686, index);
    }

    @Test
    void inactivity_degrades_the_index_through_freshness() {
        // Single result 34 months old: level = 700 (weights cancel), freshness 0.5 -> 350.
        assertEquals(350, DogRankIndex.of(List.of(result("700", 34)), NOW));
    }

    @Test
    void freshness_is_driven_by_the_most_recent_result_only() {
        // An old 800 (34 months) plus a fresh 800: freshness snaps to 1.0, level stays 800.
        assertEquals(800, DogRankIndex.of(List.of(result("800", 34), result("800", 0)), NOW));
    }

    @Test
    void no_dog_ever_disappears_thanks_to_the_floor() {
        assertEquals(7, DogRankIndex.of(List.of(result("700", 120)), NOW));
    }
}
