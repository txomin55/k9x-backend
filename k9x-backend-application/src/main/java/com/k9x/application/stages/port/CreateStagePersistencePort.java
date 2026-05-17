package com.k9x.application.stages.port;

public interface CreateStagePersistencePort {

    void createStage(String id, String name, String competitionId, Long dateFrom, Long dateTo, String creator, long createdAt);
}
