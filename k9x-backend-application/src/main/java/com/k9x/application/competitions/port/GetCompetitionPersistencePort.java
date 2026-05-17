package com.k9x.application.competitions.port;

import com.k9x.domain.aggregates.competitions.Competition;

public interface GetCompetitionPersistencePort {

    Competition getCompetition(String id);
}
