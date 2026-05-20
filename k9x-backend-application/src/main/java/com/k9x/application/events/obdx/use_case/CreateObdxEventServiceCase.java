package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.obdx.port.CreateObdxEventPersistencePort;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

public class CreateObdxEventServiceCase {

    private final GetStagePersistencePort getStagePersistencePort;
    private final CreateObdxEventPersistencePort createObdxEventPersistencePort;

    public CreateObdxEventServiceCase(GetStagePersistencePort getStagePersistencePort,
                                      CreateObdxEventPersistencePort createObdxEventPersistencePort) {
        this.getStagePersistencePort = getStagePersistencePort;
        this.createObdxEventPersistencePort = createObdxEventPersistencePort;
    }

    public void createEvent(String id, String name, String stageId, String userId, boolean organizer) {
        assertOrganizer(organizer);
        Stage stage = getStagePersistencePort.getStage(stageId);
        assertStageValidations(stage, userId);
        createObdxEventPersistencePort.createEvent(id, name, stageId, userId, DateUtils.nowUtcMillis());
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertStageValidations(Stage stage, String userId) {
        if (stage == null) {
            throw new StageNotFoundException();
        }
        if (stage.deletedAt() != null) {
            throw new StageAlreadyDeletedException();
        }
        if (!stage.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
