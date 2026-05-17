package com.k9x.application.stages.port;

import com.k9x.domain.aggregates.stages.Stage;

public interface GetStagePersistencePort {

    Stage getStage(String id);
}
