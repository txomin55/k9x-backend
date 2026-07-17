package com.k9x.domain.events.aggregates;

import com.k9x.domain.events.status.EventStatus;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.EventJudge;
import com.k9x.domain.events.valueobjects.Score;
import com.k9x.domain.shared.UtcDates;

import com.k9x.domain.disciplines.obdx.LiveExcludedExercise;
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
        List<Score> scores,
        List<String> awards,
        String rank,
        Integer rankScore
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
     * Whether enrollment is still open: an event with no deadline set never accepts enrollments (a
     * deadline must be configured first), otherwise enrollment stays open until the deadline is reached
     * (compared against the supplied current timestamp).
     */
    public boolean enrollmentOpened(long now) {
        return enrollmentDeadline != null && !UtcDates.isAfterUtcDay(now, enrollmentDeadline);
    }

    public boolean hasAnyScore() {
        return scores != null && scores.stream().anyMatch(s -> s.score() != null);
    }

    /**
     * An event is finished when it has at least one competitor and every competitor is fully scored.
     * Unlike the per-competitor LIVE/SETTLED status, this considers <em>every</em> exercise — including
     * group stays and general impression — so the event stays STARTED until those collective scores are
     * also in, even when every competitor has already left LIVE.
     */
    private boolean allCompetitorsSettled() {
        if (competitors == null || competitors.isEmpty()) {
            return false;
        }
        return competitors.stream().allMatch(c -> isSettled(c, true));
    }

    /**
     * Whether the given competitor is settled for its displayed status: flagged {@code notCompeting} or
     * holding a score from every judge assigned to each individual exercise. Exercises excluded from the
     * live status (see {@link LiveExcludedExercise}) do not count, so a competitor that has finished its
     * individual runs is settled even while the group stay / general impression are still pending.
     * Unknown dog ids are treated as not settled.
     */
    public boolean isCompetitorSettled(String dogId) {
        if (competitors == null) {
            return false;
        }
        return competitors.stream()
                .filter(c -> c.dogId().equals(dogId))
                .findFirst()
                .map(c -> isSettled(c, false))
                .orElse(false);
    }

    /**
     * Whether the given competitor has started: it holds at least one recorded score on an individual
     * exercise. Exercises excluded from the live status (see {@link LiveExcludedExercise}) — group stays
     * and general impression — never start a competitor on their own, otherwise scoring the group flight
     * would flip every competitor to LIVE regardless of their individual runs. A competitor that has not
     * started yet is neither {@code LIVE} nor {@code SETTLED} but pending.
     */
    public boolean isCompetitorStarted(String dogId) {
        return scores != null && scores.stream()
                .anyMatch(s -> s.score() != null && dogId.equals(s.dogId())
                        && !LiveExcludedExercise.isExcluded(s.exerciseId()));
    }

    /**
     * Number of yellow cards stamped for the competitor across every exercise×judge combination. Since a
     * given exercise×judge×dog can only ever hold one yellow card, this equals the number of score rows for
     * the dog with a stamped card.
     */
    public long yellowCardCount(String dogId) {
        if (scores == null) {
            return 0;
        }
        return scores.stream()
                .filter(s -> s.yellowCard() != null && dogId.equals(s.dogId()))
                .count();
    }

    /**
     * Whether the competitor already holds a red card. Only one red card can ever exist per dog in an
     * event, so this is a plain existence check rather than a count.
     */
    public boolean hasRedCard(String dogId) {
        if (scores == null) {
            return false;
        }
        return scores.stream().anyMatch(s -> s.redCard() != null && dogId.equals(s.dogId()));
    }

    /**
     * A competitor that accumulates a second yellow card, or that holds a red card, is disqualified: its
     * participation is over and it can no longer receive scores, so it is treated as settled regardless of
     * remaining exercises.
     */
    public boolean isDisqualified(String dogId) {
        return yellowCardCount(dogId) >= 2 || hasRedCard(dogId);
    }

    /**
     * Whether the competitor has been flagged as not competing. Unknown dog ids are treated as competing.
     */
    public boolean isNotCompeting(String dogId) {
        if (competitors == null) {
            return false;
        }
        return competitors.stream()
                .filter(c -> c.dogId().equals(dogId))
                .findFirst()
                .map(EventCompetitor::notCompeting)
                .orElse(false);
    }

    /**
     * Whether the competitor holds a score from every judge assigned to each relevant exercise of the
     * event (per-exercise judge assignment, e.g. only the judges of that exercise's ring). When
     * {@code includeExcludedExercises} is {@code false} the exercises excluded from the live status (see
     * {@link LiveExcludedExercise}) — group stays and general impression — are ignored, so a competitor
     * that has finished its individual runs counts as settled even while those collective scores are
     * still pending; this drives the displayed competitor status. When {@code true} every exercise must
     * be scored, which is what marks the whole event FINISHED. An event with no relevant exercise is
     * treated as never settled.
     */
    private boolean isSettled(EventCompetitor competitor, boolean includeExcludedExercises) {
        if (competitor.notCompeting() || isDisqualified(competitor.dogId())) {
            return true;
        }
        if (exercises == null || exercises.isEmpty()) {
            return false;
        }
        List<EventExercise> relevantExercises = includeExcludedExercises ? exercises : exercises.stream()
                .filter(e -> !LiveExcludedExercise.isExcluded(e.exerciseId()))
                .toList();
        if (relevantExercises.isEmpty()) {
            return false;
        }
        Set<String> scoredPairs = scores == null ? Set.of() : scores.stream()
                .filter(s -> s.score() != null && competitor.dogId().equals(s.dogId()))
                .map(s -> s.exerciseId() + "|" + s.judgeId())
                .collect(Collectors.toSet());
        return relevantExercises.stream().allMatch(exercise -> {
            List<String> assignedJudges = exercise.judges();
            return assignedJudges == null || assignedJudges.stream()
                    .allMatch(judgeId -> scoredPairs.contains(exercise.exerciseId() + "|" + judgeId));
        });
    }
}
