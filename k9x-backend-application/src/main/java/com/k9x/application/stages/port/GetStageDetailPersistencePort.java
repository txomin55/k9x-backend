package com.k9x.application.stages.port;

import com.k9x.application.stages.use_case.dto.FetchStageDetailDTO;

public interface GetStageDetailPersistencePort {

    FetchStageDetailDTO getStage(String id);
}
