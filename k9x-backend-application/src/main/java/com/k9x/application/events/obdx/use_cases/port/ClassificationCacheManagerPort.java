package com.k9x.application.events.obdx.use_cases.port;

import com.k9x.application.events.obdx.use_cases.dto.FetchObdxClassificationDTO;

public interface ClassificationCacheManagerPort {

    FetchObdxClassificationDTO getIfPresentAndValid(String eventId, int ttlSeconds);

    void put(String eventId, FetchObdxClassificationDTO dto);
}
