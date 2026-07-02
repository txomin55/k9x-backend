package com.k9x.application.collections.obdx.port;

import com.k9x.application.collections.obdx.use_case.dto.FetchObdxRedCardDTO;

public interface GetObdxRedCardPersistencePort {

    FetchObdxRedCardDTO getRedCard(String eventId, String competitorId);
}
