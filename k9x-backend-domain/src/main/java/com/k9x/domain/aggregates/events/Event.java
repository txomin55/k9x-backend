package com.k9x.domain.aggregates.events;

import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record Event(
        String id,
        String configurationId,
        String discipline,
        String name,
        String stageId,
        String creator,
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
     * recorded scores: an event is FINISHED once every competitor is settled, STARTED once any score has
     * been taken, otherwise CREATED.
     */
    public EventStatus status() {
        if (deletedAt != null) {
            return EventStatus.DELETED;
        }
        if (allCompetitorsSettled()) {
            return EventStatus.FINISHED;
        }
        if (hasAnyScore()) {
            return EventStatus.STARTED;
        }
        return EventStatus.CREATED;
    }

    private boolean hasAnyScore() {
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
        int required = (exercises == null ? 0 : exercises.size()) * (judges == null ? 0 : judges.size());
        return competitors.stream().allMatch(c -> isSettled(c, required));
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
