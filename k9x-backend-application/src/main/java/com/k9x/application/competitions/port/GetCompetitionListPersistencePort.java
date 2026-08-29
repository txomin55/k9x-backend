package com.k9x.application.competitions.port;

import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;

import java.util.List;

public interface GetCompetitionListPersistencePort {

    /** Hydrates the competition root aggregates owned by the creator, of that country when one is given. */
    List<CompetitionSnapshot> getCompetitions(String creator, String country);
}
