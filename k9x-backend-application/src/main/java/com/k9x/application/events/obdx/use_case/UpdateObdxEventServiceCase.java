package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventConfigurationIdRequiredException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.UpdateObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.payload.UpdateObdxEventPersistencePayload;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxEventCommand;
import com.k9x.domain.aggregates.events.obdx.ObdxEvent;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class UpdateObdxEventServiceCase {

    private final GetObdxEventPersistencePort getObdxEventPersistencePort;
    private final UpdateObdxEventPersistencePort updateObdxEventPersistencePort;

    public UpdateObdxEventServiceCase(GetObdxEventPersistencePort getObdxEventPersistencePort,
                                      UpdateObdxEventPersistencePort updateObdxEventPersistencePort) {
        this.getObdxEventPersistencePort = getObdxEventPersistencePort;
        this.updateObdxEventPersistencePort = updateObdxEventPersistencePort;
    }

    public void updateEvent(String id, UpdateObdxEventCommand command, String userId, boolean organizer) {
        assertOrganizer(organizer);
        assertConfigurationId(command.configurationId());
        ObdxEvent obdxEvent = getObdxEventPersistencePort.getEvent(id);
        assertEventValidations(obdxEvent, userId);
        updateObdxEventPersistencePort.updateEvent(id, UpdateObdxEventPersistencePayload.from(command));
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertConfigurationId(String configurationId) {
        if (configurationId == null || configurationId.isBlank()) {
            throw new EventConfigurationIdRequiredException();
        }
    }

    private void assertEventValidations(ObdxEvent obdxEvent, String userId) {
        if (obdxEvent == null) {
            throw new EventNotFoundException();
        }
        if (obdxEvent.deletedAt() != null) {
            throw new EventAlreadyDeletedException();
        }
        if (!obdxEvent.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
