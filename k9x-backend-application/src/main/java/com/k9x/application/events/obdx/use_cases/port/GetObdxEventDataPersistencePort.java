package com.k9x.application.events.obdx.use_cases.port;

import com.k9x.application.events.obdx.use_cases.dto.FetchObdxEventDataDTO;

public interface GetObdxEventDataPersistencePort {
    FetchObdxEventDataDTO getEventData(String eventId);
}
