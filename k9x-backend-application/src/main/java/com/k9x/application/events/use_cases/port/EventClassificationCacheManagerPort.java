package com.k9x.application.events.use_cases.port;

import com.k9x.application.events.use_cases.dto.EventClassificationContextDTO;

public interface EventClassificationCacheManagerPort {

    EventClassificationContextDTO getIfPresentAndValid(String eventId, int ttlSeconds);

    void put(String eventId, EventClassificationContextDTO context);
}
