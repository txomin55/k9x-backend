package com.k9x.application.events.use_cases;

import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.DeleteObdxEventPersistencePort;
import com.k9x.application.events.obdx.use_cases.port.GetEventPersistencePort;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class DeleteEventServiceCase {

    private final GetEventPersistencePort getEventPersistencePort;
    private final GetStagePersistencePort getStagePersistencePort;
    private final DeleteObdxEventPersistencePort deleteObdxEventPersistencePort;

    public DeleteEventServiceCase(GetEventPersistencePort getEventPersistencePort,
                                  GetStagePersistencePort getStagePersistencePort,
                                  DeleteObdxEventPersistencePort deleteObdxEventPersistencePort) {
        this.getEventPersistencePort = getEventPersistencePort;
        this.getStagePersistencePort = getStagePersistencePort;
        this.deleteObdxEventPersistencePort = deleteObdxEventPersistencePort;
    }

    public void deleteEvent(String id, String userId, boolean organizer) {
        assertOrganizer(organizer);
        Event event = getEventPersistencePort.getEvent(id);
        assertEventValidations(event);
        Stage stage = getStagePersistencePort.getStage(event.stageId());
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
