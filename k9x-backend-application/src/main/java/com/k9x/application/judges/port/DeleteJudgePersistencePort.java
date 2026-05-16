package com.k9x.application.judges.port;

public interface DeleteJudgePersistencePort {

    void deleteJudge(String id, long deletedAt);
}
