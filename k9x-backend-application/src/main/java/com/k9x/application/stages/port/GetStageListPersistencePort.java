package com.k9x.application.stages.port;

import com.k9x.application.stages.use_case.dto.FetchStageListDTO;

import java.util.List;

public interface GetStageListPersistencePort {

    List<FetchStageListDTO> getStages();
}
