package com.k9x.domain.dogs.rank;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DogRankDistributionTest {

    @Test
    void covers_the_whole_charted_scale_with_empty_bands_included() {
        List<DogRankDistribution.Bucket> buckets = DogRankDistribution.buckets(Map.of());

        assertEquals(45, buckets.size());
        assertEquals(new DogRankDistribution.Bucket(100, 120, 0), buckets.get(0));
        assertEquals(new DogRankDistribution.Bucket(980, 1000, 0), buckets.get(44));
    }

    @Test
    void folds_each_index_into_its_band_and_ignores_the_ones_below_the_threshold() {
        List<DogRankDistribution.Bucket> buckets = DogRankDistribution.buckets(
                Map.of(99, 7, 100, 2, 119, 1, 120, 3, 1000, 1, 980, 1));

        assertEquals(3, buckets.get(0).dogs());
        assertEquals(3, buckets.get(1).dogs());
        // a perfect 1000 shares the last band instead of opening one of its own
        assertEquals(2, buckets.get(44).dogs());
        assertEquals(8, buckets.stream().mapToInt(DogRankDistribution.Bucket::dogs).sum());
    }

    @Test
    void charts_only_dogs_at_or_above_the_threshold() {
        assertTrue(DogRankDistribution.charted(100));
        assertFalse(DogRankDistribution.charted(99));
    }

    @Test
    void position_counts_the_dogs_strictly_ahead_so_ties_share_it() {
        Map<Integer, Integer> field = Map.of(900, 1, 800, 2, 700, 1);

        assertEquals(1, DogRankDistribution.position(field, 900));
        assertEquals(2, DogRankDistribution.position(field, 800));
        assertEquals(4, DogRankDistribution.position(field, 700));
    }

    @Test
    void top_percent_rounds_up_so_the_leader_is_never_top_zero() {
        assertEquals(1, DogRankDistribution.topPercent(8, 1_623_014));
        assertEquals(1, DogRankDistribution.topPercent(1, 100));
        assertEquals(2, DogRankDistribution.topPercent(2, 100));
        assertEquals(2, DogRankDistribution.topPercent(1, 50));
        assertEquals(34, DogRankDistribution.topPercent(1, 3));
        assertEquals(100, DogRankDistribution.topPercent(50, 50));
    }

    @Test
    void top_percent_rejects_a_position_outside_the_field() {
        assertThrows(IllegalArgumentException.class, () -> DogRankDistribution.topPercent(0, 10));
        assertThrows(IllegalArgumentException.class, () -> DogRankDistribution.topPercent(11, 10));
    }
}
