package com.k9x.application.competitions.port;

public interface UpdateCompetitionPersistencePort {

    void updateCompetition(String id, String name, String description, String address,
                           Double coordAlt, Double coordLong, long lastUpdate);
}
