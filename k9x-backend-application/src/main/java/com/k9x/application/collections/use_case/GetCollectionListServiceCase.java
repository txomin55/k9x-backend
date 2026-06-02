package com.k9x.application.collections.use_case;

import com.k9x.application.collections.port.GetCollectionListPersistencePort;
import com.k9x.application.collections.use_case.dto.FetchCollectionDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.collections.CollectionStatus;

import java.util.List;

public class GetCollectionListServiceCase {

    private final GetCollectionListPersistencePort getCollectionListPersistencePort;

    public GetCollectionListServiceCase(GetCollectionListPersistencePort getCollectionListPersistencePort) {
        this.getCollectionListPersistencePort = getCollectionListPersistencePort;
    }

    public List<FetchCollectionDTO> getCollections(String userId) {
        return getCollectionListPersistencePort.getCollections(userId, DateUtils.nowUtcMillis()).stream()
                .map(c -> new FetchCollectionDTO(c.eventId(), c.eventName(), c.stageName(),
                        c.competitionName(), c.discipline(), CollectionStatus.OPEN.name(), c.judges()))
                .toList();
    }
}
