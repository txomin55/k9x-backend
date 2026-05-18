package com.k9x.application.judges.port;

import com.k9x.application.judges.port.payload.UpdateJudgePersistencePayload;

public interface UpdateJudgePersistencePort {

    void updateJudge(String id, UpdateJudgePersistencePayload payload);
}
