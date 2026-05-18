package com.k9x.application.stages.port;

import com.k9x.application.stages.payload.UpdateStagePersistencePayload;

public interface UpdateStagePersistencePort {

    void updateStage(String id, UpdateStagePersistencePayload payload);
}
