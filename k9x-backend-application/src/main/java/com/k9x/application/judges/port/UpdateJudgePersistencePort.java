package com.k9x.application.judges.port;

public interface UpdateJudgePersistencePort {

    void updateJudge(String id, String name, long lastUpdate);
}
