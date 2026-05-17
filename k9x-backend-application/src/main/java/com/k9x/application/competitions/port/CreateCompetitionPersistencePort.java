package com.k9x.application.competitions.port;

public interface CreateCompetitionPersistencePort {

    void createCompetition(String id, String name, String creator, long createdAt);
}
