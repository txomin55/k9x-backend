package com.k9x.domain.dogs.rank;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DogRankIndexTimelineTest {

    private static final long START = 1_700_000_000_000L;

    private static long month(double months) {
        return DogRankIndex.plusMonths(START, months);
    }

    private static DogRankIndex.Result result(String score, double atMonth) {
        return new DogRankIndex.Result(new BigDecimal(score), month(atMonth));
    }

    private static List<Long> timestamps(List<DogRankIndexTimeline.Point> curve) {
        return curve.stream().map(DogRankIndexTimeline.Point::timestamp).toList();
    }

    @Test
    void an_empty_history_has_no_curve() {
        assertTrue(DogRankIndexTimeline.of(List.of(), month(10)).isEmpty());
    }

    @Test
    void every_point_away_from_an_event_is_the_index_the_cron_would_compute_at_that_instant() {
        List<DogRankIndex.Result> results = List.of(result("750", 0), result("600", 10));

        List<DogRankIndexTimeline.Point> curve = DogRankIndexTimeline.of(results, month(30));

        assertTrue(curve.size() > 30);
        curve.stream().filter(point -> point.timestamp() != month(10)).forEach(point -> {
            List<DogRankIndex.Result> known = results.stream()
                    .filter(result -> result.timestamp() <= point.timestamp()).toList();
            assertEquals(DogRankIndex.of(known, point.timestamp()), point.index());
        });
    }

    @Test
    void starts_at_the_first_result_and_ends_now_in_time_order() {
        List<DogRankIndexTimeline.Point> curve = DogRankIndexTimeline.of(List.of(result("750", 0)), month(20));

        assertEquals(new DogRankIndexTimeline.Point(month(0), 384), curve.get(0));
        assertEquals(month(20), curve.get(curve.size() - 1).timestamp());
        List<Long> timestamps = timestamps(curve);
        assertEquals(timestamps.stream().sorted().toList(), timestamps);
    }

    /** Two points share the event instant — before and after — so the line draws the jump as a vertical step. */
    @Test
    void draws_each_later_event_as_a_before_and_after_pair() {
        List<DogRankIndex.Result> results = List.of(result("300", 0), result("900", 10));

        List<Integer> atEvent = DogRankIndexTimeline.of(results, month(12)).stream()
                .filter(point -> point.timestamp() == month(10))
                .map(DogRankIndexTimeline.Point::index)
                .toList();

        assertEquals(List.of(DogRankIndex.of(List.of(result("300", 0)), month(10)),
                DogRankIndex.of(results, month(10))), atEvent);
    }

    /** The plateau ends are exact samples, not cut by the monthly grid, so the corner of the line is sharp. */
    @Test
    void samples_the_end_of_both_plateaus() {
        List<Long> timestamps = timestamps(DogRankIndexTimeline.of(List.of(result("750", 0.5)), month(40)));

        assertTrue(timestamps.contains(DogRankIndex.freshnessDegradationFrom(month(0.5))));
        assertTrue(timestamps.contains(DogRankIndex.plusMonths(month(0.5), DogRankIndex.LEVEL_PLATEAU_MONTHS_THRESHOLD)));
    }

    /** Past the freshness floor nothing moves: the tail is closed by a single point at now. */
    @Test
    void stops_sampling_at_the_freshness_floor() {
        List<DogRankIndexTimeline.Point> curve = DogRankIndexTimeline.of(List.of(result("750", 0)), month(200));

        long floor = month(DogRankIndex.FRESHNESS_FLOOR_MONTHS_THRESHOLD);
        assertEquals(List.of(month(200)), timestamps(curve).stream().filter(timestamp -> timestamp > floor).toList());
        assertEquals(curve.get(curve.size() - 2).index(), curve.get(curve.size() - 1).index());
    }
}
