package com.k9x.application.competitions.port;

import com.k9x.domain.competitions.aggregates.CompetitionAggregate;

/**
 * Single write port for the competition root aggregate. The adapter persists the aggregate by
 * replaying its {@link CompetitionAggregate#pendingChanges()}, emitting only the affected SQL.
 */
public interface SaveCompetitionPersistencePort {

    void save(CompetitionAggregate competition);
}
