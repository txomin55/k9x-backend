package com.k9x.application.collections.port;

import com.k9x.application.collections.use_case.dto.FetchCollectionCompetitorDTO;

import java.util.List;

public interface GetCollectionCompetitorsPersistencePort {
    List<FetchCollectionCompetitorDTO> getCompetitors(String eventId);
}
