package com.k9x.application.collections.obdx.use_case;

import com.k9x.application.collections.obdx.port.GetObdxRedCardPersistencePort;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxRedCardDTO;

public class GetObdxRedCardServiceCase {

    private final GetObdxRedCardPersistencePort getObdxRedCardPersistencePort;

    public GetObdxRedCardServiceCase(GetObdxRedCardPersistencePort getObdxRedCardPersistencePort) {
        this.getObdxRedCardPersistencePort = getObdxRedCardPersistencePort;
    }

    public FetchObdxRedCardDTO getRedCard(String eventId, String competitorId) {
        return getObdxRedCardPersistencePort.getRedCard(eventId, competitorId);
    }
}
