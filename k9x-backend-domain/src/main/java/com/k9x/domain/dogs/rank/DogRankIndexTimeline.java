package com.k9x.domain.dogs.rank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * The dog's index as a curve over time, sampled with the very {@link DogRankIndex} formula the history cron uses,
 * so a chart drawn from it lands on the same figures the directory shows.
 *
 * <p>The cron only records the index at events and, afterwards, once per crossed month of inactivity; the level
 * curve that erodes old results between two events never reaches the history. Sampling here is what lets the
 * chart draw that erosion. The curve holds:
 *
 * <ul>
 *   <li>a point every month from the first result on;</li>
 *   <li>a point at every age where one of the two curves changes slope, for every result, so the corners of the
 *       line are exact rather than cut by the monthly grid;</li>
 *   <li>at every event instant <em>two</em> points with the same timestamp — the index just before the result
 *       and just after it — so a line through the curve draws the jump as a vertical step;</li>
 *   <li>a last point at {@code now}.</li>
 * </ul>
 *
 * Sampling stops moving at the freshness floor month: past it neither curve changes, so the tail is flat and a
 * single point at {@code now} closes it.
 */
public final class DogRankIndexTimeline {

    /** One sample of the curve: the index as of {@code timestamp}. */
    public record Point(long timestamp, int index) {
    }

    private DogRankIndexTimeline() {
    }

    /**
     * @param results the dog's rank history, in any order; empty yields an empty curve.
     * @param now     current epoch millis: the curve ends here.
     */
    public static List<Point> of(List<DogRankIndex.Result> results, long now) {
        if (results.isEmpty()) {
            return List.of();
        }
        List<DogRankIndex.Result> sorted = results.stream()
                .sorted(Comparator.comparingLong(DogRankIndex.Result::timestamp))
                .toList();
        long first = sorted.get(0).timestamp();
        long last = sorted.get(sorted.size() - 1).timestamp();
        long end = Math.max(last, now);
        long flatFrom = Math.min(end, DogRankIndex.plusMonths(last, DogRankIndex.FRESHNESS_FLOOR_MONTHS_THRESHOLD));

        TreeSet<Long> eventInstants = new TreeSet<>();
        sorted.forEach(result -> eventInstants.add(result.timestamp()));

        TreeSet<Long> samples = new TreeSet<>();
        for (int month = 1; DogRankIndex.plusMonths(first, month) < flatFrom; month++) {
            samples.add(DogRankIndex.plusMonths(first, month));
        }
        for (long instant : eventInstants) {
            for (double month : DogRankIndex.slopeChangeMonths()) {
                long sample = DogRankIndex.plusMonths(instant, month);
                if (sample < flatFrom) {
                    samples.add(sample);
                }
            }
        }
        samples.add(flatFrom);
        samples.add(end);
        samples.removeAll(eventInstants);

        List<Point> curve = new ArrayList<>();
        for (long instant : eventInstants) {
            // Everything strictly between the previous event and this one, then this one's before/after pair.
            Long previous = eventInstants.lower(instant);
            samples.subSet(previous == null ? Long.MIN_VALUE : previous, false, instant, false)
                    .forEach(sample -> curve.add(pointAt(sorted, sample)));
            List<DogRankIndex.Result> before = upTo(sorted, instant, false);
            if (!before.isEmpty()) {
                curve.add(new Point(instant, DogRankIndex.of(before, instant)));
            }
            curve.add(pointAt(sorted, instant));
        }
        samples.tailSet(last, false).forEach(sample -> curve.add(pointAt(sorted, sample)));
        return curve;
    }

    private static Point pointAt(List<DogRankIndex.Result> sorted, long instant) {
        return new Point(instant, DogRankIndex.of(upTo(sorted, instant, true), instant));
    }

    private static List<DogRankIndex.Result> upTo(List<DogRankIndex.Result> sorted, long instant, boolean inclusive) {
        return sorted.stream()
                .filter(result -> inclusive ? result.timestamp() <= instant : result.timestamp() < instant)
                .toList();
    }
}
