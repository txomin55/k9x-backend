package com.k9x.domain.events.aggregates;

import com.k9x.domain.events.status.EventStatus;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.EventJudge;
import com.k9x.domain.events.valueobjects.Score;
import com.k9x.domain.shared.UtcDates;

import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record EventSnapshot(
        String id,
        String configurationId,
        String discipline,
        String name,
        String stageId,
        String creator,
        Long enrollmentDeadline,
        long lastUpdate,
        long createdAt,
        Long deletedAt,
        ObdxAvgMethod scoreCalculation,
        List<EventCompetitor> competitors,
        List<EventExercise> exercises,
        List<EventJudge> judges,
        List<Score> scores
) {

    /**
     * Lifecycle status. "Pooling" is only a front-end label, so the backend derives the status from the
     * recorded scores: an event is FINISHED once every competitor is settled or once its stage's
     * {@code dateTo} day has passed, STARTED once any score has been taken, otherwise CREATED.
     */
    public EventStatus status(long now, long stageDateTo) {
        if (deletedAt != null) {
            return EventStatus.DELETED;
        }
        if (UtcDates.isAfterUtcDay(now, stageDateTo)) {
            return EventStatus.FINISHED;
        }
        if (allCompetitorsSettled()) {
            return EventStatus.FINISHED;
        }
        if (hasAnyScore()) {
            return EventStatus.STARTED;
        }
        return EventStatus.CREATED;
    }

    /**
     * Whether enrollment is still open: an event with no deadline is always open, otherwise enrollment
     * stays open until the deadline is reached (compared against the supplied current timestamp).
     */
    public boolean enrollmentOpened(long now) {
        return enrollmentDeadline == null || !UtcDates.isAfterUtcDay(now, enrollmentDeadline);
    }

    public boolean hasAnyScore() {
        return scores != null && scores.stream().anyMatch(s -> s.score() != null);
    }

    /**
     * An event is finished when it has at least one competitor and every competitor is settled. A
     * competitor is settled when it is flagged {@code notCompeting} or holds a score for every
     * exercise×judge combination of the event.
     */
    private boolean allCompetitorsSettled() {
        if (competitors == null || competitors.isEmpty()) {
            return false;
        }
        int required = requiredScores();
        return competitors.stream().allMatch(c -> isSettled(c, required));
    }

    /**
     * Whether the given competitor is settled: flagged {@code notCompeting} or holding a score for every
     * exercise×judge combination of the event. Unknown dog ids are treated as not settled.
     */
    public boolean isCompetitorSettled(String dogId) {
        if (competitors == null) {
            return false;
        }
        return competitors.stream()
                .filter(c -> c.dogId().equals(dogId))
                .findFirst()
                .map(c -> isSettled(c, requiredScores()))
                .orElse(false);
    }

    /**
     * Whether the given competitor has started: it holds at least one recorded score. A competitor that
     * has not started yet is neither {@code LIVE} nor {@code SETTLED} but pending.
     */
    public boolean isCompetitorStarted(String dogId) {
        return scores != null && scores.stream()
                .anyMatch(s -> s.score() != null && dogId.equals(s.dogId()));
    }

    private int requiredScores() {
        return (exercises == null ? 0 : exercises.size()) * (judges == null ? 0 : judges.size());
    }

    private boolean isSettled(EventCompetitor competitor, int requiredScores) {
        if (competitor.notCompeting()) {
            return true;
        }
        if (requiredScores == 0) {
            return false;
        }
        Set<String> scoredPairs = scores == null ? Set.of() : scores.stream()
                .filter(s -> s.score() != null && competitor.dogId().equals(s.dogId()))
                .map(s -> s.exerciseId() + "|" + s.judgeId())
                .collect(Collectors.toSet());
        return scoredPairs.size() >= requiredScores;
    }
}
