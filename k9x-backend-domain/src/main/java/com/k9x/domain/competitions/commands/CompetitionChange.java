package com.k9x.domain.competitions.commands;

/**
 * Domain event recorded by a {@link com.k9x.domain.competitions.aggregates.CompetitionAggregate}
 * mutation, describing a single change to apply to persistence. The persistence adapter dispatches
 * over the concrete type to emit only the affected SQL — no tree diffing, no full-tree rewrite.
 */
public sealed interface CompetitionChange permits
        CompetitionCreated, CompetitionUpdated, CompetitionDeleted,
        StageCreated, StageRenamed, StageDeleted,
        EventCreated, EventDeleted, DogEnrolled,
        ObdxEventInfoUpdated, ScoreUpdated, CompetitorNotCompetingUpdated {
}
