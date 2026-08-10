package com.k9x.domain.rankings.results;

import com.k9x.domain.rankings.RankingGroupBy;
import com.k9x.domain.rankings.RankingIncludeBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns the classifications of N events into the ranked groups of a ranking.
 *
 * <p>All the ranking arithmetic lives here, as a pure function of its inputs, so it can be reasoned about
 * and tested without a database or a classification pipeline.
 *
 * <p>The rules it encodes:
 * <ul>
 *   <li>A group is a competitor, a team or a country, depending on {@link RankingGroupBy}.</li>
 *   <li>A competitor with no score in an event still gets a cell, with a {@code null} score, so the matrix
 *       stays rectangular and the UI can render it disabled.</li>
 *   <li>{@link RankingIncludeBy} applies to the group's <em>pooled</em> results (competitor × event): the
 *       best or worst {@code includedCount} of them are what add up. That keeps the count comparable
 *       between groups of different sizes.</li>
 *   <li>Groups are ordered by total, best first; equal totals share a position and are flagged as tied.</li>
 *   <li>Competitors entered as reserves are dropped entirely unless the ranking includes them.</li>
 * </ul>
 */
public final class RankingAggregation {

    private RankingAggregation() {}

    /** A pooled result of a group, used to decide which scores count. */
    private record PooledResult(String dogIdentification, String eventId, BigDecimal score) {}

    public static List<RankingGroup> aggregate(List<String> eventIds,
                                              List<RankingEventResults> eventResults,
                                              RankingGroupBy groupBy,
                                              RankingIncludeBy includeBy,
                                              Integer includedCount,
                                              boolean includeReserves) {
        Map<String, RankingCompetitorResult> latestByDog = new LinkedHashMap<>();
        Map<String, Map<String, RankingCompetitorResult>> byEvent = new LinkedHashMap<>();
        eventResults.forEach(event -> {
            Map<String, RankingCompetitorResult> competitorsByDog = new LinkedHashMap<>();
            event.competitors().forEach(competitor -> {
                // Reserves are dropped up front when the ranking excludes them, so they neither form a group
                // nor contribute a cell anywhere.
                if (!includeReserves && competitor.reserve()) {
                    return;
                }
                competitorsByDog.put(competitor.dogIdentification(), competitor);
                latestByDog.put(competitor.dogIdentification(), competitor);
            });
            byEvent.put(event.eventId(), competitorsByDog);
        });

        Map<String, List<RankingCompetitorResult>> membersByGroup = groupCompetitors(latestByDog, groupBy);

        List<RankingGroup> groups = membersByGroup.entrySet().stream()
                .map(entry -> buildGroup(entry.getKey(), entry.getValue(), eventIds, byEvent, groupBy,
                        includeBy, includedCount))
                .sorted(Comparator.comparing(RankingGroup::total).reversed())
                .toList();

        return assignPositions(groups);
    }

    /**
     * Buckets competitors by the grouping criterion, keeping first-seen order.
     *
     * <p>A blank team leaves a competitor out of a team ranking: there is no team to rank it under, and
     * inventing an empty group would put unrelated competitors together.
     */
    private static Map<String, List<RankingCompetitorResult>> groupCompetitors(
            Map<String, RankingCompetitorResult> competitorsByDog, RankingGroupBy groupBy) {
        Map<String, List<RankingCompetitorResult>> grouped = new LinkedHashMap<>();
        competitorsByDog.values().forEach(competitor -> {
            String key = groupKey(competitor, groupBy);
            if (key == null || key.isBlank()) {
                return;
            }
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(competitor);
        });
        return grouped;
    }

    private static String groupKey(RankingCompetitorResult competitor, RankingGroupBy groupBy) {
        return switch (groupBy) {
            case INDIVIDUAL -> competitor.dogIdentification();
            case TEAM -> competitor.team();
            case COUNTRY -> competitor.country();
        };
    }

    private static String groupName(String key, List<RankingCompetitorResult> members, RankingGroupBy groupBy) {
        return groupBy == RankingGroupBy.INDIVIDUAL ? members.getFirst().dogName() : key;
    }

    private static RankingGroup buildGroup(String key,
                                           List<RankingCompetitorResult> members,
                                           List<String> eventIds,
                                           Map<String, Map<String, RankingCompetitorResult>> byEvent,
                                           RankingGroupBy groupBy,
                                           RankingIncludeBy includeBy,
                                           Integer includedCount) {
        List<PooledResult> pooled = new ArrayList<>();
        members.forEach(member -> eventIds.forEach(eventId -> {
            RankingCompetitorResult inEvent = byEvent
                    .getOrDefault(eventId, Map.of())
                    .get(member.dogIdentification());
            if (inEvent != null && inEvent.hasScore()) {
                pooled.add(new PooledResult(member.dogIdentification(), eventId, inEvent.totalScore()));
            }
        }));

        List<PooledResult> counting = selectCounting(pooled, includeBy, includedCount);
        BigDecimal total = counting.stream()
                .map(PooledResult::score)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RankingMember> rows = members.stream()
                .map(member -> new RankingMember(
                        member.dogIdentification(),
                        member.dogName(),
                        buildCells(member, eventIds, byEvent, counting)))
                .toList();

        return new RankingGroup(key, groupName(key, members, groupBy), 0, false, total, rows);
    }

    private static List<RankingCell> buildCells(RankingCompetitorResult member,
                                                List<String> eventIds,
                                                Map<String, Map<String, RankingCompetitorResult>> byEvent,
                                                List<PooledResult> counting) {
        return eventIds.stream()
                .map(eventId -> {
                    RankingCompetitorResult inEvent = byEvent
                            .getOrDefault(eventId, Map.of())
                            .get(member.dogIdentification());
                    boolean hasScore = inEvent != null && inEvent.hasScore();
                    boolean counts = hasScore && counting.stream().anyMatch(result ->
                            result.dogIdentification().equals(member.dogIdentification())
                                    && result.eventId().equals(eventId));
                    return new RankingCell(eventId, hasScore ? inEvent.totalScore() : null, counts);
                })
                .toList();
    }

    private static List<PooledResult> selectCounting(List<PooledResult> pooled,
                                                     RankingIncludeBy includeBy,
                                                     Integer includedCount) {
        if (includeBy.includesAll() || includedCount == null || includedCount >= pooled.size()) {
            return pooled;
        }
        Comparator<PooledResult> byScore = Comparator.comparing(PooledResult::score);
        return pooled.stream()
                .sorted(includeBy == RankingIncludeBy.HIGHEST ? byScore.reversed() : byScore)
                .limit(includedCount)
                .toList();
    }

    /**
     * Stamps positions on an already sorted list: equal totals share a position and are flagged as tied,
     * and the next distinct total resumes at the ordinal it would have had.
     */
    private static List<RankingGroup> assignPositions(List<RankingGroup> sorted) {
        List<RankingGroup> positioned = new ArrayList<>(sorted.size());
        int position = 0;
        for (int index = 0; index < sorted.size(); index++) {
            RankingGroup group = sorted.get(index);
            boolean sameAsPrevious = index > 0
                    && sorted.get(index - 1).total().compareTo(group.total()) == 0;
            if (!sameAsPrevious) {
                position = index + 1;
            }
            boolean tiedWithNext = index + 1 < sorted.size()
                    && sorted.get(index + 1).total().compareTo(group.total()) == 0;
            positioned.add(new RankingGroup(group.id(), group.name(), position,
                    sameAsPrevious || tiedWithNext, group.total(), group.members()));
        }
        return positioned;
    }
}
