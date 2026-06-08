package com.k9x.application.competitions.port;

import com.k9x.domain.aggregates.competitions.Competition;

import java.util.List;

public interface GetCompetitionListPersistencePort {

    /** Hydrates every competition root aggregate owned by the given creator. */
    List<Competition> getCompetitions(String creator);
}
