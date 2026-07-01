package com.k9x.application.judges.port;

public interface CreateJudgePersistencePort {

    void createJudge(String id, String name, String country, String creator, long createdAt);
}
