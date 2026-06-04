package com.k9x.application.collections.obdx.port;

import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;

import java.util.List;

public interface GetObdxCollectionEventJudgesPersistencePort {
    List<FetchCollectionJudgeWithCollectorDTO> getJudges(String eventId);
}
