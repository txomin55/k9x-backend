package com.k9x.application.events.obdx.use_cases;

import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventConfigurationIdRequiredException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.port.UpdateObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.payload.UpdateObdxEventPersistencePayload;
import com.k9x.application.events.obdx.use_cases.command.UpdateObdxEventCommand;
import com.k9x.application.events.obdx.use_cases.port.GetEventPersistencePort;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class UpdateObdxEventServiceCase {

    private final GetEventPersistencePort getEventPersistencePort;
    private final UpdateObdxEventPersistencePort updateObdxEventPersistencePort;
    private final GetObdxClassificationConfigPort getObdxClassificationConfigPort;

    public UpdateObdxEventServiceCase(GetEventPersistencePort getEventPersistencePort,
                                      UpdateObdxEventPersistencePort updateObdxEventPersistencePort,
                                      GetObdxClassificationConfigPort getObdxClassificationConfigPort) {
        this.getEventPersistencePort = getEventPersistencePort;
        this.updateObdxEventPersistencePort = updateObdxEventPersistencePort;
        this.getObdxClassificationConfigPort = getObdxClassificationConfigPort;
    }

    public void updateEvent(String id, UpdateObdxEventCommand command, String userId, boolean organizer) {
        assertOrganizer(organizer);
        assertConfigurationId(command.configurationId());
        Event event = getEventPersistencePort.getEvent(id);
        assertEventValidations(event, userId);
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
}
