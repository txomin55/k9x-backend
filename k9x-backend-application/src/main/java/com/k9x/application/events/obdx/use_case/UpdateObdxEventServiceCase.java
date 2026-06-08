package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventConfigurationIdRequiredException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxCollectorNotFoundException;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.port.UpdateObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.payload.UpdateObdxEventPersistencePayload;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxEventCommand;
import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class UpdateObdxEventServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final UpdateObdxEventPersistencePort updateObdxEventPersistencePort;
    private final GetObdxClassificationConfigPort getObdxClassificationConfigPort;
    private final GetUserInfoPersistencePort getUserInfoPersistencePort;

    public UpdateObdxEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                      UpdateObdxEventPersistencePort updateObdxEventPersistencePort,
                                      GetObdxClassificationConfigPort getObdxClassificationConfigPort,
                                      GetUserInfoPersistencePort getUserInfoPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.updateObdxEventPersistencePort = updateObdxEventPersistencePort;
        this.getObdxClassificationConfigPort = getObdxClassificationConfigPort;
        this.getUserInfoPersistencePort = getUserInfoPersistencePort;
    }

    public void updateEvent(String id, UpdateObdxEventCommand command, String userId, boolean organizer) {
        assertOrganizer(organizer);
        assertConfigurationId(command.configurationId());
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(id);
        if (competitionId == null) throw new EventNotFoundException();
        Competition competition = getCompetitionPersistencePort.getCompetition(competitionId);
        Event event = CompetitionNavigator.findEvent(competition, id);
        assertEventValidations(event, userId);
        assertCollectorsExist(command);
        ObdxAvgMethod scoreCalculation = getObdxClassificationConfigPort
                .getConfig(command.configurationId())
                .cacheEvictStrategy()
                .getAvgMethod();
        updateObdxEventPersistencePort.updateEvent(id, UpdateObdxEventPersistencePayload.from(command, scoreCalculation));
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) throw new UnauthorizedResourceException();
    }

    private void assertConfigurationId(String configurationId) {
        if (configurationId == null || configurationId.isBlank()) throw new EventConfigurationIdRequiredException();
    }

    private void assertEventValidations(Event event, String userId) {
        if (event == null) throw new EventNotFoundException();
        if (event.deletedAt() != null) throw new EventAlreadyDeletedException();
        if (!event.creator().equals(userId)) throw new UnauthorizedResourceException();
    }

    private void assertCollectorsExist(UpdateObdxEventCommand command) {
        command.judges().stream()
                .map(UpdateObdxEventCommand.JudgeCommand::collectorEmail)
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .forEach(email -> {
                    if (getUserInfoPersistencePort.findById(email) == null) {
                        throw new ObdxCollectorNotFoundException(email);
                    }
                });
    }
}
