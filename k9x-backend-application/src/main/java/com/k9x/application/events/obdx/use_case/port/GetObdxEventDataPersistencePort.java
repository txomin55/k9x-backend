package com.k9x.application.events.obdx.use_case.port;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDataDTO;

public interface GetObdxEventDataPersistencePort {
    FetchObdxEventDataDTO getEventData(String eventId);
}
