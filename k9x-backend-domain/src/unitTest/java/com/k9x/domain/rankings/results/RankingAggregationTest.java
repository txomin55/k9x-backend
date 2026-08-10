package com.k9x.domain.rankings.results;

import com.k9x.domain.rankings.RankingGroupBy;
import com.k9x.domain.rankings.RankingIncludeBy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingAggregationTest {

    private static final List<String> TWO_EVENTS = List.of("event-1", "event-2");

    private static RankingCompetitorResult competitor(String dog, String team, String country, String score) {
        return new RankingCompetitorResult(dog, dog.toUpperCase(), team, country,
                score == null ? null : new BigDecimal(score), false, false);
    }

    private static RankingCompetitorResult notCompeting(String dog, String team, String country, String score) {
        return new RankingCompetitorResult(dog, dog.toUpperCase(), team, country, new BigDecimal(score), true,
                false);
    }

    private static RankingCompetitorResult reserve(String dog, String team, String country, String score) {
        return new RankingCompetitorResult(dog, dog.toUpperCase(), team, country, new BigDecimal(score), false,
                true);
    }

    private static List<RankingGroup> aggregate(List<RankingEventResults> events,
                                                RankingGroupBy groupBy,
                                                RankingIncludeBy includeBy,
                                                Integer includedCount) {
        return RankingAggregation.aggregate(TWO_EVENTS, events, groupBy, includeBy, includedCount, true);
    }

    private static List<RankingGroup> aggregateWithoutReserves(List<RankingEventResults> events,
                                                              RankingGroupBy groupBy) {
        return RankingAggregation.aggregate(TWO_EVENTS, events, groupBy, RankingIncludeBy.ALL, null, false);
    }

    private static void assertTotal(String expected, RankingGroup group) {
        assertEquals(0, new BigDecimal(expected).compareTo(group.total()),
                () -> "expected total " + expected + " but was " + group.total());
    }

    private static List<String> ids(List<RankingGroup> groups) {
        return groups.stream().map(RankingGroup::id).toList();
    }

    @Test
    void sums_every_event_of_a_competitor_when_all_results_count() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(competitor("rex", "alpha", "ES", "100"))),
                new RankingEventResults("event-2", List.of(competitor("rex", "alpha", "ES", "80")))
        ), RankingGroupBy.INDIVIDUAL, RankingIncludeBy.ALL, null);

        assertEquals(1, groups.size());
        assertTotal("180", groups.getFirst());
        assertEquals("REX", groups.getFirst().name());
    }

    @Test
    void orders_groups_by_total_best_first_and_stamps_positions() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(
                        competitor("rex", "alpha", "ES", "50"),
                        competitor("luna", "beta", "FR", "90"))),
                new RankingEventResults("event-2", List.of(
                        competitor("rex", "alpha", "ES", "50"),
                        competitor("luna", "beta", "FR", "90")))
        ), RankingGroupBy.INDIVIDUAL, RankingIncludeBy.ALL, null);

        assertEquals(List.of("luna", "rex"), ids(groups));
        assertEquals(1, groups.get(0).position());
        assertEquals(2, groups.get(1).position());
        assertFalse(groups.get(0).tied());
        assertFalse(groups.get(1).tied());
    }

    @Test
    void shares_the_position_and_flags_both_groups_when_totals_tie() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(
                        competitor("rex", "alpha", "ES", "70"),
                        competitor("luna", "beta", "FR", "70"))),
                new RankingEventResults("event-2", List.of())
        ), RankingGroupBy.INDIVIDUAL, RankingIncludeBy.ALL, null);

        assertEquals(1, groups.get(0).position());
        assertEquals(1, groups.get(1).position());
        assertTrue(groups.get(0).tied());
        assertTrue(groups.get(1).tied());
    }

    @Test
    void resumes_numbering_after_a_tie() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(
                        competitor("rex", "alpha", "ES", "70"),
                        competitor("luna", "beta", "FR", "70"),
                        competitor("toby", "gamma", "PT", "10"))),
                new RankingEventResults("event-2", List.of())
        ), RankingGroupBy.INDIVIDUAL, RankingIncludeBy.ALL, null);

        assertEquals(1, groups.get(0).position());
        assertEquals(1, groups.get(1).position());
        assertEquals(3, groups.get(2).position());
        assertFalse(groups.get(2).tied());
    }

    @Test
    void leaves_a_null_cell_for_an_event_the_competitor_did_not_enter() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(competitor("rex", "alpha", "ES", "100"))),
                new RankingEventResults("event-2", List.of())
        ), RankingGroupBy.INDIVIDUAL, RankingIncludeBy.ALL, null);

        List<RankingCell> cells = groups.getFirst().members().getFirst().cells();
        assertEquals(List.of("event-1", "event-2"), cells.stream().map(RankingCell::eventId).toList());
        assertEquals(0, new BigDecimal("100").compareTo(cells.getFirst().score()));
        assertTrue(cells.getFirst().counts());
        assertNull(cells.get(1).score());
        assertFalse(cells.get(1).counts());
    }

    @Test
    void treats_a_competitor_marked_as_not_competing_as_absent() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(notCompeting("rex", "alpha", "ES", "100"))),
                new RankingEventResults("event-2", List.of(competitor("rex", "alpha", "ES", "40")))
        ), RankingGroupBy.INDIVIDUAL, RankingIncludeBy.ALL, null);

        assertTotal("40", groups.getFirst());
        assertNull(groups.getFirst().members().getFirst().cells().getFirst().score());
    }

    @Test
    void counts_only_the_best_results_of_the_group_when_including_the_highest() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(competitor("rex", "alpha", "ES", "30"))),
                new RankingEventResults("event-2", List.of(competitor("rex", "alpha", "ES", "90")))
        ), RankingGroupBy.INDIVIDUAL, RankingIncludeBy.HIGHEST, 1);

        assertTotal("90", groups.getFirst());
        List<RankingCell> cells = groups.getFirst().members().getFirst().cells();
        // The discarded score is still shown, it just does not count.
        assertEquals(0, new BigDecimal("30").compareTo(cells.getFirst().score()));
        assertFalse(cells.getFirst().counts());
        assertTrue(cells.get(1).counts());
    }

    @Test
    void counts_only_the_worst_results_of_the_group_when_including_the_lowest() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(competitor("rex", "alpha", "ES", "30"))),
                new RankingEventResults("event-2", List.of(competitor("rex", "alpha", "ES", "90")))
        ), RankingGroupBy.INDIVIDUAL, RankingIncludeBy.LOWEST, 1);

        assertTotal("30", groups.getFirst());
    }

    @Test
    void counts_everything_when_the_included_count_exceeds_the_available_results() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(competitor("rex", "alpha", "ES", "30"))),
                new RankingEventResults("event-2", List.of(competitor("rex", "alpha", "ES", "90")))
        ), RankingGroupBy.INDIVIDUAL, RankingIncludeBy.HIGHEST, 5);

        assertTotal("120", groups.getFirst());
    }

    @Test
    void pools_the_results_of_every_member_when_grouping_by_team() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(
                        competitor("rex", "alpha", "ES", "100"),
                        competitor("nala", "alpha", "ES", "50"),
                        competitor("luna", "beta", "FR", "80"))),
                new RankingEventResults("event-2", List.of())
        ), RankingGroupBy.TEAM, RankingIncludeBy.ALL, null);

        assertEquals(List.of("alpha", "beta"), ids(groups));
        assertTotal("150", groups.getFirst());
        assertEquals(List.of("rex", "nala"),
                groups.getFirst().members().stream().map(RankingMember::id).toList());
    }

    @Test
    void takes_the_best_results_of_the_whole_team_not_of_each_member() {
        // Pooled: rex 100, nala 50, rex 10 -> the best two are 100 and 50, which belong to different members.
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(
                        competitor("rex", "alpha", "ES", "100"),
                        competitor("nala", "alpha", "ES", "50"))),
                new RankingEventResults("event-2", List.of(
                        competitor("rex", "alpha", "ES", "10")))
        ), RankingGroupBy.TEAM, RankingIncludeBy.HIGHEST, 2);

        assertTotal("150", groups.getFirst());
        RankingMember rex = groups.getFirst().members().getFirst();
        assertTrue(rex.cells().getFirst().counts());
        assertFalse(rex.cells().get(1).counts());
    }

    @Test
    void groups_by_country_code() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(
                        competitor("rex", "alpha", "ES", "40"),
                        competitor("luna", "beta", "ES", "40"),
                        competitor("toby", "gamma", "FR", "70"))),
                new RankingEventResults("event-2", List.of())
        ), RankingGroupBy.COUNTRY, RankingIncludeBy.ALL, null);

        assertEquals(List.of("ES", "FR"), ids(groups));
        assertTotal("80", groups.getFirst());
    }

    @Test
    void leaves_competitors_without_a_team_out_of_a_team_ranking() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(
                        competitor("rex", "", "ES", "100"),
                        competitor("luna", "beta", "FR", "10"))),
                new RankingEventResults("event-2", List.of())
        ), RankingGroupBy.TEAM, RankingIncludeBy.ALL, null);

        assertEquals(List.of("beta"), ids(groups));
    }

    @Test
    void keeps_a_competitor_with_no_score_at_all_as_a_group_with_zero() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(competitor("rex", "alpha", "ES", null))),
                new RankingEventResults("event-2", List.of())
        ), RankingGroupBy.INDIVIDUAL, RankingIncludeBy.ALL, null);

        assertEquals(1, groups.size());
        assertTotal("0", groups.getFirst());
        groups.getFirst().members().getFirst().cells()
                .forEach(cell -> assertNull(cell.score()));
    }

    @Test
    void returns_no_groups_when_there_are_no_events() {
        assertTrue(RankingAggregation.aggregate(List.of(), List.of(),
                RankingGroupBy.INDIVIDUAL, RankingIncludeBy.ALL, null, true).isEmpty());
    }

    @Test
    void counts_reserves_when_the_ranking_includes_them() {
        List<RankingGroup> groups = aggregate(List.of(
                new RankingEventResults("event-1", List.of(reserve("rex", "alpha", "ES", "100"))),
                new RankingEventResults("event-2", List.of())
        ), RankingGroupBy.INDIVIDUAL, RankingIncludeBy.ALL, null);

        assertEquals(List.of("rex"), ids(groups));
        assertTotal("100", groups.getFirst());
    }

    @Test
    void drops_reserves_entirely_when_the_ranking_excludes_them() {
        List<RankingGroup> groups = aggregateWithoutReserves(List.of(
                new RankingEventResults("event-1", List.of(
                        reserve("rex", "alpha", "ES", "100"),
                        competitor("luna", "beta", "FR", "10"))),
                new RankingEventResults("event-2", List.of())
        ), RankingGroupBy.INDIVIDUAL);

        assertEquals(List.of("luna"), ids(groups));
    }

    @Test
    void a_reserve_leaves_no_cell_behind_in_its_team_when_excluded() {
        List<RankingGroup> groups = aggregateWithoutReserves(List.of(
                new RankingEventResults("event-1", List.of(
                        reserve("rex", "alpha", "ES", "100"),
                        competitor("nala", "alpha", "ES", "50"))),
                new RankingEventResults("event-2", List.of())
        ), RankingGroupBy.TEAM);

        assertEquals(List.of("alpha"), ids(groups));
        assertTotal("50", groups.getFirst());
        assertEquals(List.of("nala"),
                groups.getFirst().members().stream().map(RankingMember::id).toList());
    }
}
