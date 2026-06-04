package com.k9x.application.events.obdx.use_cases;

import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxScoreNotAllowedException;
import com.k9x.application.events.obdx.exceptions.ObdxUserNotCollectorException;
import com.k9x.application.events.obdx.port.GetObdxEventCollectorPersistencePort;
import com.k9x.application.events.obdx.port.UpdateObdxScorePersistencePort;
import com.k9x.application.events.obdx.port.payload.UpdateObdxScorePersistencePayload;
import com.k9x.application.events.obdx.use_cases.command.UpdateObdxScoreCommand;
import com.k9x.application.events.obdx.use_cases.port.GetEventPersistencePort;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;

import java.math.BigDecimal;
import java.util.List;

public class UpdateObdxScoreServiceCase {

    private final GetEventPersistencePort getEventPersistencePort;
    private final GetStagePersistencePort getStagePersistencePort;
    private final GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort;
    private final GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort;
    private final UpdateObdxScorePersistencePort updateObdxScorePersistencePort;

    public UpdateObdxScoreServiceCase(GetEventPersistencePort getEventPersistencePort,
                                      GetStagePersistencePort getStagePersistencePort,
                                      GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort,
                                      GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort,
                                      UpdateObdxScorePersistencePort updateObdxScorePersistencePort) {
        this.getEventPersistencePort = getEventPersistencePort;
        this.getStagePersistencePort = getStagePersistencePort;
        this.getObdxEventCollectorPersistencePort = getObdxEventCollectorPersistencePort;
        this.getObdxExerciseAllowedValuesPort = getObdxExerciseAllowedValuesPort;
        this.updateObdxScorePersistencePort = updateObdxScorePersistencePort;
    }

    public void updateScore(String eventId, UpdateObdxScoreCommand command, String userEmail) {
        Event event = getEventPersistencePort.getEvent(eventId);
        assertEventValidations(event);
        Stage stage = getStagePersistencePort.getStage(event.stageId());
        assertStageNotExpired(stage);
        assertUserIsCollector(eventId, command.judgeId(), userEmail);
        assertScoreAllowed(command.exerciseId(), command.score());
        updateObdxScorePersistencePort.updateScore(eventId, UpdateObdxScorePersistencePayload.from(command));
    }

    private void assertEventValidations(Event event) {
        if (event == null) throw new EventNotFoundException();
        if (event.deletedAt() != null) throw new EventAlreadyDeletedException();
    }

    private void assertStageNotExpired(Stage stage) {
        if (stage.dateTo() < DateUtils.nowUtcMillis()) throw new StageExpiredException();
    }

    private void assertUserIsCollector(String eventId, String judgeId, String userEmail) {
        String collectorId = getObdxEventCollectorPersistencePort.getCollectorId(eventId, judgeId);
        if (collectorId == null || !collectorId.equals(userEmail)) throw new ObdxUserNotCollectorException();
    }

    private void assertScoreAllowed(String exerciseId, BigDecimal score) {
        List<BigDecimal> allowedValues = getObdxExerciseAllowedValuesPort.getAllowedValues(exerciseId);
        if (score == null || allowedValues.stream().noneMatch(v -> v.compareTo(score) == 0)) {
            throw new ObdxScoreNotAllowedException();
        }
    }
}
