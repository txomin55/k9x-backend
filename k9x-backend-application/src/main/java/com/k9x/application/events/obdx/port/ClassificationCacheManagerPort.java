package com.k9x.application.events.obdx.port;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;

public interface ClassificationCacheManagerPort {

    FetchClassificationDTO getIfPresentAndValid(String eventId, int ttlSeconds);

    void put(String eventId, FetchClassificationDTO dto);
}
