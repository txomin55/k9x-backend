package com.k9x.application.stages.port;

public interface UpdateStagePersistencePort {

    void updateStage(String id, String name, Long dateFrom, Long dateTo, long lastUpdate);
}
