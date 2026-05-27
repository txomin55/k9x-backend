package com.k9x.application.events.obdx.port;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;

import java.util.List;

public interface GetObdxEventListPersistencePort {

    List<FetchObdxEventDTO> getEvents(List<String> stageIds);
}
