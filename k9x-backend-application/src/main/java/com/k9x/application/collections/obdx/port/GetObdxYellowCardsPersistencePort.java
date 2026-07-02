package com.k9x.application.collections.obdx.port;

import com.k9x.application.collections.obdx.use_case.dto.FetchObdxYellowCardDTO;

import java.util.List;

public interface GetObdxYellowCardsPersistencePort {

    List<FetchObdxYellowCardDTO> getYellowCards(String eventId, String competitorId);
}
