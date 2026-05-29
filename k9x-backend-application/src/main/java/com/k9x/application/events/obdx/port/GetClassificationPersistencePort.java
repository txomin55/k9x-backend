package com.k9x.application.events.obdx.port;

import com.k9x.application.events.obdx.use_case.dto.FetchClassificationRawRowDTO;

import java.util.List;

public interface GetClassificationPersistencePort {

    List<FetchClassificationRawRowDTO> getClassification(String eventId);
}
