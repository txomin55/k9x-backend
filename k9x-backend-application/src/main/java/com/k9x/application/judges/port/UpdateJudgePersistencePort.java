package com.k9x.application.judges.port;

import com.k9x.application.judges.payload.UpdateJudgePersistencePayload;

public interface UpdateJudgePersistencePort {

    void updateJudge(String id, UpdateJudgePersistencePayload payload);
}
