package com.k9x.application.collections.obdx.use_case;

import com.k9x.application.collections.obdx.use_case.command.RegisterObdxYellowCardCommand;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.events.obdx.exceptions.ObdxUserNotCollectorException;
import com.k9x.application.events.obdx.port.GetObdxEventCollectorPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.competitions.commands.YellowCardData;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.shared.SupportUser;

public class RegisterObdxYellowCardServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    public RegisterObdxYellowCardServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                             GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort,
                                             SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.getObdxEventCollectorPersistencePort = getObdxEventCollectorPersistencePort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
    }

    public void registerYellowCard(String eventId, RegisterObdxYellowCardCommand command, String userEmail) {
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(eventId);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        if (!SupportUser.is(userEmail)) {
            assertUserIsCollector(eventId, command.judgeId(), userEmail);
        }
        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));
        competition.registerYellowCard(eventId,
                new YellowCardData(command.judgeId(), command.exerciseId(), command.dogId()),
                userEmail, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }

    private void assertUserIsCollector(String eventId, String judgeId, String userEmail) {
        String collectorId = getObdxEventCollectorPersistencePort.getCollectorId(eventId, judgeId);
        if (collectorId == null || !collectorId.equals(userEmail)) {
            throw new ObdxUserNotCollectorException();
        }
    }
}
