package com.k9x.application.collections.port;

import com.k9x.application.collections.use_case.dto.FetchCollectionDTO;

import java.util.List;

public interface GetCollectionListPersistencePort {
    List<FetchCollectionDTO> getCollections(String collectorEmail, long nowMillis);
}
