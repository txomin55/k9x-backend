package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.GetSelectableCompetitionsPersistencePort;
import com.k9x.application.competitions.use_case.dto.FetchSelectableCompetitionDTO;
import com.k9x.application.utils.auth.AuthAssertions;

import java.util.List;

/**
 * Catalogue of competitions, trials and events for the ranking editor. Not scoped by creator: a ranking may
 * group events from competitions the organizer did not create.
 */
public class GetSelectableCompetitionListServiceCase {

    private final GetSelectableCompetitionsPersistencePort getSelectableCompetitionsPersistencePort;

    public GetSelectableCompetitionListServiceCase(
            GetSelectableCompetitionsPersistencePort getSelectableCompetitionsPersistencePort) {
        this.getSelectableCompetitionsPersistencePort = getSelectableCompetitionsPersistencePort;
    }

    public List<FetchSelectableCompetitionDTO> getSelectableCompetitions(String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        return getSelectableCompetitionsPersistencePort.getSelectableCompetitions();
    }
}
