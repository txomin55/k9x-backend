package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.competitions.commands.NewStageData;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.shared.SupportUser;

public class CreateStageServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    public CreateStageServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
    }

    public void createStage(String id, String name, String competitionId, Long dateFrom, Long dateTo,
                            String userId, boolean organizer) {
        assertOrganizer(organizer, userId);

        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));

        competition.createStage(new NewStageData(id, name, dateFrom, dateTo), userId, DateUtils.nowUtcMillis());

        saveCompetitionPersistencePort.save(competition);
    }

    private void assertOrganizer(boolean organizer, String userId) {
        if (!organizer && !SupportUser.is(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
