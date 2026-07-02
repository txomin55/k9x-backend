package com.k9x.application.collections.obdx.use_case;

import com.k9x.application.collections.obdx.port.GetObdxYellowCardsPersistencePort;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxYellowCardDTO;

import java.util.List;

public class GetObdxYellowCardsServiceCase {

    private final GetObdxYellowCardsPersistencePort getObdxYellowCardsPersistencePort;

    public GetObdxYellowCardsServiceCase(GetObdxYellowCardsPersistencePort getObdxYellowCardsPersistencePort) {
        this.getObdxYellowCardsPersistencePort = getObdxYellowCardsPersistencePort;
    }

    public List<FetchObdxYellowCardDTO> getYellowCards(String eventId, String competitorId) {
        return getObdxYellowCardsPersistencePort.getYellowCards(eventId, competitorId);
    }
}
