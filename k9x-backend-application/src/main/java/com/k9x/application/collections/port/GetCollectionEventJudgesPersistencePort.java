package com.k9x.application.collections.port;

import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;

import java.util.List;

public interface GetCollectionEventJudgesPersistencePort {
    List<FetchCollectionJudgeWithCollectorDTO> getJudges(String eventId);
}
