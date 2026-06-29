package com.k9x.application.stages.port;

import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;

import java.util.List;

public interface GetStageListPersistencePort {

    /**
     * Hydrates every competition root aggregate (stages → events → scores) backing the global stage list.
     * The service case flattens these into the per-stage read-model and computes lifecycle status from the
     * hydrated events, so STARTED (driven by event scores) is surfaced exactly here.
     */
    List<CompetitionSnapshot> getCompetitions();
}
