package com.k9x.application.collections.obdx.port;

import com.k9x.application.collections.use_case.dto.FetchCollectionCompetitorDTO;

import java.util.List;

public interface GetObdxCollectionCompetitorsPersistencePort {
    List<FetchCollectionCompetitorDTO> getCompetitors(String eventId);
}
