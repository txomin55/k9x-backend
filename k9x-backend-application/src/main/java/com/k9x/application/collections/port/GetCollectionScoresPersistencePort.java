package com.k9x.application.collections.port;

import com.k9x.application.collections.use_case.dto.FetchCollectionScoreDTO;

import java.util.List;

public interface GetCollectionScoresPersistencePort {
    List<FetchCollectionScoreDTO> getScores(String eventId);
}
