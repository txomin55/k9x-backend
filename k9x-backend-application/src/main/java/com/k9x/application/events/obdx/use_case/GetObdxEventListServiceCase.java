package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.obdx.port.GetObdxEventListPersistencePort;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

import java.util.List;

public class GetObdxEventListServiceCase {

    private final GetStagePersistencePort getStagePersistencePort;
    private final GetObdxEventListPersistencePort getObdxEventListPersistencePort;

    public GetObdxEventListServiceCase(GetStagePersistencePort getStagePersistencePort,
                                       GetObdxEventListPersistencePort getObdxEventListPersistencePort) {
        this.getStagePersistencePort = getStagePersistencePort;
        this.getObdxEventListPersistencePort = getObdxEventListPersistencePort;
    }

    public List<FetchObdxEventDTO> getEvents(List<String> stageIds, String userId, boolean organizer) {
        assertOrganizer(organizer);
        stageIds.forEach(stageId -> assertStageValidations(getStagePersistencePort.getStage(stageId), userId));
        return getObdxEventListPersistencePort.getEvents(stageIds);
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
