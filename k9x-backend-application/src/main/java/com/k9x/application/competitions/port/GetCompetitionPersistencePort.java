package com.k9x.application.competitions.port;

import com.k9x.domain.aggregates.competitions.Competition;

public interface GetCompetitionPersistencePort {

    /** Hydrates the full competition root aggregate (stages → events → competitors/exercises/judges/scores). */
    Competition getCompetition(String id);

    /** Resolves the owning competition id of a stage, or {@code null} when the stage does not exist. */
    String competitionIdByStage(String stageId);

    /** Resolves the owning competition id of an event, or {@code null} when the event does not exist. */
    String competitionIdByEvent(String eventId);
}
