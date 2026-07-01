package com.k9x.application.events.obdx.port;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxYellowCardDTO;

import java.util.List;

public interface GetObdxYellowCardsPersistencePort {

    List<FetchObdxYellowCardDTO> getYellowCards(String eventId, String competitorId);
}
