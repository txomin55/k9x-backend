package com.k9x.application.competitions.port;

import com.k9x.application.competitions.use_case.dto.FetchCompetitionDTO;

import java.util.List;

public interface GetCompetitionListPersistencePort {

    List<FetchCompetitionDTO> getCompetitions(String creator);
}
