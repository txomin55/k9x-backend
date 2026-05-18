package com.k9x.application.competitions.port;

public interface DeleteCompetitionPersistencePort {

    void deleteCompetition(String id, long deletedAt);
}
