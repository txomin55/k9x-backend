package com.k9x.application.events.use_case;

import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventCannotBeDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.DeleteObdxEventPersistencePort;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.events.EventStatus;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class DeleteEventServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final DeleteObdxEventPersistencePort deleteObdxEventPersistencePort;

    public DeleteEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                  DeleteObdxEventPersistencePort deleteObdxEventPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.deleteObdxEventPersistencePort = deleteObdxEventPersistencePort;
    }

    public void deleteEvent(String id, String userId, boolean organizer) {
        assertOrganizer(organizer);
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(id);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        Competition competition = getCompetitionPersistencePort.getCompetition(competitionId);
        Event event = CompetitionNavigator.findEvent(competition, id);
        assertEventValidations(event);
        Stage stage = CompetitionNavigator.findStageOfEvent(competition, id);
        assertStageValidations(stage, userId);
        deleteObdxEventPersistencePort.deleteEvent(id, DateUtils.nowUtcMillis());
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertEventValidations(Event event) {
        if (event == null) {
            throw new EventNotFoundException();
        }
        if (event.deletedAt() != null) {
            throw new EventAlreadyDeletedException();
        }
        EventStatus status = event.status();
        if (status == EventStatus.STARTED || status == EventStatus.FINISHED) {
            throw new EventCannotBeDeletedException();
        }
    }

    private void assertStageValidations(Stage stage, String userId) {
        if (stage.deletedAt() != null) {
            throw new StageAlreadyDeletedException();
        }
        if (!stage.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
