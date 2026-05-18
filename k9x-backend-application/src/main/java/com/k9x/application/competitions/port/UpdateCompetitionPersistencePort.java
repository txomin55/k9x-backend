package com.k9x.application.competitions.port;

import com.k9x.application.competitions.port.payload.UpdateCompetitionPersistencePayload;

public interface UpdateCompetitionPersistencePort {

    void updateCompetition(String id, UpdateCompetitionPersistencePayload payload);
}
