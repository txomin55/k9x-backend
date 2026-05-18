package com.k9x.application.stages.port;

public interface DeleteStagePersistencePort {

    void deleteStage(String id, long deletedAt);
}
