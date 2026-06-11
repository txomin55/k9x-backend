package com.k9x.application.competitions.port;

import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;

import java.util.List;

public interface GetCompetitionListPersistencePort {

    /** Hydrates every competition root aggregate owned by the given creator. */
    List<CompetitionSnapshot> getCompetitions(String creator);
}
