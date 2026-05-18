package com.k9x.application.competitions.port;

import com.k9x.application.competitions.dto.FetchCompetitionDTO;

import java.util.List;

public interface GetCompetitionListPersistencePort {

    List<FetchCompetitionDTO> getCompetitions(String creator);
}
