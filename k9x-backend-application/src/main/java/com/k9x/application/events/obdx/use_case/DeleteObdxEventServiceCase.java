package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.DeleteObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.events.obdx.ObdxEvent;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class DeleteObdxEventServiceCase {

    private final GetObdxEventPersistencePort getObdxEventPersistencePort;
    private final GetStagePersistencePort getStagePersistencePort;
    private final DeleteObdxEventPersistencePort deleteObdxEventPersistencePort;

    public DeleteObdxEventServiceCase(GetObdxEventPersistencePort getObdxEventPersistencePort,
                                      GetStagePersistencePort getStagePersistencePort,
                                      DeleteObdxEventPersistencePort deleteObdxEventPersistencePort) {
        this.getObdxEventPersistencePort = getObdxEventPersistencePort;
        this.getStagePersistencePort = getStagePersistencePort;
        this.deleteObdxEventPersistencePort = deleteObdxEventPersistencePort;
    }

    public void deleteEvent(String id, String userId, boolean organizer) {
        assertOrganizer(organizer);
        ObdxEvent obdxEvent = getObdxEventPersistencePort.getEvent(id);
        assertEventValidations(obdxEvent);
        Stage stage = getStagePersistencePort.getStage(obdxEvent.stageId());
        assertStageValidations(stage, userId);
        deleteObdxEventPersistencePort.deleteEvent(id, DateUtils.nowUtcMillis());
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertEventValidations(ObdxEvent obdxEvent) {
        if (obdxEvent == null) {
            throw new EventNotFoundException();
        }
        if (obdxEvent.deletedAt() != null) {
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
