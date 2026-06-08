package com.k9x.application.events.use_case;

import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.EnrollObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.payload.EnrollObdxEventPersistencePayload;
import com.k9x.application.events.obdx.use_case.command.EnrollObdxEventCommand;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;

public class EnrollEventServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final EnrollObdxEventPersistencePort enrollObdxEventPersistencePort;

    public EnrollEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  EnrollObdxEventPersistencePort enrollObdxEventPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.enrollObdxEventPersistencePort = enrollObdxEventPersistencePort;
    }

    public void enrollEvent(String eventId, EnrollObdxEventCommand command) {
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(eventId);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        Competition competition = getCompetitionPersistencePort.getCompetition(competitionId);
        Event event = CompetitionNavigator.findEvent(competition, eventId);
        assertEventValidations(event);
        Stage stage = CompetitionNavigator.findStageOfEvent(competition, eventId);
        assertStageNotExpired(stage);
        enrollObdxEventPersistencePort.enrollEvent(eventId, EnrollObdxEventPersistencePayload.from(command));
    }

    private void assertEventValidations(Event event) {
        if (event == null) throw new EventNotFoundException();
        if (event.deletedAt() != null) throw new EventAlreadyDeletedException();
    }

    private void assertStageNotExpired(Stage stage) {
        if (stage.dateTo() < DateUtils.nowUtcMillis()) throw new StageExpiredException();
    }
}
