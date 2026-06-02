package com.k9x.application.events.use_cases;

import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.EnrollObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.payload.EnrollObdxEventPersistencePayload;
import com.k9x.application.events.obdx.use_cases.command.EnrollObdxEventCommand;
import com.k9x.application.events.obdx.use_cases.port.GetEventPersistencePort;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;

public class EnrollEventServiceCase {

    private final GetEventPersistencePort getEventPersistencePort;
    private final GetStagePersistencePort getStagePersistencePort;
    private final EnrollObdxEventPersistencePort enrollObdxEventPersistencePort;

    public EnrollEventServiceCase(GetEventPersistencePort getEventPersistencePort,
                                  GetStagePersistencePort getStagePersistencePort,
                                  EnrollObdxEventPersistencePort enrollObdxEventPersistencePort) {
        this.getEventPersistencePort = getEventPersistencePort;
        this.getStagePersistencePort = getStagePersistencePort;
        this.enrollObdxEventPersistencePort = enrollObdxEventPersistencePort;
    }

    public void enrollEvent(String eventId, EnrollObdxEventCommand command) {
        Event event = getEventPersistencePort.getEvent(eventId);
        assertEventValidations(event);
        Stage stage = getStagePersistencePort.getStage(event.stageId());
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
