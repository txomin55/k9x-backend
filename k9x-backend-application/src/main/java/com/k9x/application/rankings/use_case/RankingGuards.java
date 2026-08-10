package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.use_case.command.SaveRankingCommand;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.rankings.aggregates.Ranking;
import com.k9x.domain.rankings.exceptions.RankingDuplicateEventException;
import com.k9x.domain.rankings.exceptions.RankingEventNotAvailableException;
import com.k9x.domain.rankings.exceptions.RankingEventsRequiredException;
import com.k9x.domain.rankings.exceptions.RankingIncludedCountRequiredException;
import com.k9x.domain.rankings.exceptions.RankingNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared write guards for rankings, so the validation in save/delete lives in one place.
 */
public final class RankingGuards {

    private RankingGuards() {}

    /**
     * A ranking can only be replaced or deleted by its creator.
     *
     * <p>Unlike the other entities, a {@code null} is legitimate here: the save operation is an upsert,
     * so not finding a row simply means this is a brand new ranking. Existence is asserted separately by
     * {@link #assertExists(Ranking)}, which only the delete path needs.
     */
    public static void assertMutableBy(Ranking ranking, String userId) {
        if (ranking != null && !ranking.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }

    public static void assertExists(Ranking ranking) {
        if (ranking == null) {
            throw new RankingNotFoundException();
        }
    }

    /**
     * Shape of the request on its own: at least one event, no repeats, and an included count that makes
     * sense for the chosen inclusion criterion.
     */
    public static void assertValidConfiguration(SaveRankingCommand command) {
        List<String> eventIds = command.eventIds();
        if (eventIds == null || eventIds.isEmpty()) {
            throw new RankingEventsRequiredException();
        }
        if (new HashSet<>(eventIds).size() != eventIds.size()) {
            throw new RankingDuplicateEventException();
        }
        if (!command.includeBy().includesAll()
                && (command.includedCount() == null || command.includedCount() < 1)) {
            throw new RankingIncludedCountRequiredException();
        }
    }

    /**
     * A ranking cannot exist without at least one event that is not deleted, so every requested event
     * must resolve to an existing, active one.
     */
    public static void assertEventsAreActive(List<String> eventIds, Set<String> activeEventIds) {
        if (eventIds.isEmpty()) {
            throw new RankingEventsRequiredException();
        }
        if (!activeEventIds.containsAll(eventIds)) {
            throw new RankingEventNotAvailableException();
        }
    }
}
