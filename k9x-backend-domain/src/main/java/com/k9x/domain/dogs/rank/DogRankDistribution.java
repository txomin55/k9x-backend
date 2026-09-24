package com.k9x.domain.dogs.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The public world ranking chart: how the dogs spread over the K9X index. Only dogs at or above
 * {@value #MIN_CHARTED_INDEX} are charted — below it the curve would be a long tail of dogs faded by inactivity
 * that says nothing about the competitive field. The threshold only shapes the chart: the index itself and the
 * ranking snapshot keep every dog.
 *
 * <p>Everything is derived from one per-index dog count of the field, so the bands, the total and a dog's
 * position always agree with each other.
 */
public final class DogRankDistribution {

    /** Lowest index a dog must have to be charted. */
    public static final int MIN_CHARTED_INDEX = 100;

    /** Width of each band of the chart, in index points. */
    public static final int BUCKET_WIDTH = 20;

    /** Top of the index scale, folded into the last band so a perfect 1000 does not open a band of its own. */
    public static final int MAX_INDEX = 1000;

    private DogRankDistribution() {
    }

    /** A band of the chart: {@code [from, to)}, the last one closed at {@value #MAX_INDEX}. */
    public record Bucket(int from, int to, int dogs) {
    }

    /**
     * Folds a per-index dog count into the chart's consecutive bands, lowest first. Every band from
     * {@value #MIN_CHARTED_INDEX} to {@value #MAX_INDEX} is returned, empty ones included, so the chart keeps its
     * scale whatever the field; counts below the threshold are ignored.
     */
    public static List<Bucket> buckets(Map<Integer, Integer> dogsByIndex) {
        int bands = (MAX_INDEX - MIN_CHARTED_INDEX) / BUCKET_WIDTH;
        int[] dogs = new int[bands];
        dogsByIndex.forEach((index, count) -> {
            if (index >= MIN_CHARTED_INDEX && index <= MAX_INDEX) {
                dogs[Math.min((index - MIN_CHARTED_INDEX) / BUCKET_WIDTH, bands - 1)] += count;
            }
        });
        List<Bucket> buckets = new ArrayList<>(bands);
        for (int band = 0; band < bands; band++) {
            int from = MIN_CHARTED_INDEX + band * BUCKET_WIDTH;
            buckets.add(new Bucket(from, from + BUCKET_WIDTH, dogs[band]));
        }
        return buckets;
    }

    /** Whether a dog with this index is on the chart. */
    public static boolean charted(int index) {
        return index >= MIN_CHARTED_INDEX;
    }

    /**
     * The dog's 1-based position in the field: one plus the dogs with a strictly better index, so tied dogs share
     * it — the same tie rule as an event's classification.
     */
    public static int position(Map<Integer, Integer> dogsByIndex, int index) {
        return 1 + dogsByIndex.entrySet().stream()
                .filter(entry -> entry.getKey() > index)
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    /**
     * The smallest whole percentage of the field the dog is in the top of: {@code ceil(position × 100 / total)},
     * so the leader of a big field reads "top 1%" rather than "top 0%", and the last dog "top 100%".
     */
    public static int topPercent(int position, int total) {
        if (position < 1 || total < position) {
            throw new IllegalArgumentException("Position " + position + " is outside a field of " + total);
        }
        return (int) Math.ceilDiv(position * 100L, total);
    }
}
