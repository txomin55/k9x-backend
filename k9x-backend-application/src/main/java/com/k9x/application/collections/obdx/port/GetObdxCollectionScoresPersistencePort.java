package com.k9x.application.collections.obdx.port;

import com.k9x.application.collections.use_case.dto.FetchCollectionScoreDTO;

import java.util.List;

public interface GetObdxCollectionScoresPersistencePort {
    List<FetchCollectionScoreDTO> getScores(String eventId);
}
