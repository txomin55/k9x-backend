package com.k9x.application.competitions.port;

import com.k9x.application.competitions.use_case.dto.FetchSelectableCompetitionDTO;

import java.util.List;

public interface GetSelectableCompetitionsPersistencePort {

    /**
     * Every competition that is not deleted, with its trials and events, ids and names only.
     */
    List<FetchSelectableCompetitionDTO> getSelectableCompetitions();
}
