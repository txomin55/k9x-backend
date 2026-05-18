package com.k9x.application.stages.port;

import com.k9x.application.stages.port.payload.UpdateStagePersistencePayload;

public interface UpdateStagePersistencePort {

    void updateStage(String id, UpdateStagePersistencePayload payload);
}
