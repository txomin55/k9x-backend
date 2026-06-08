package com.k9x.application.events.obdx.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxScoreNotAllowedException;
import com.k9x.application.events.obdx.exceptions.ObdxUserNotCollectorException;
import com.k9x.application.events.obdx.port.GetObdxEventCollectorPersistencePort;
import com.k9x.application.events.obdx.port.UpdateObdxScorePersistencePort;
import com.k9x.application.events.obdx.port.payload.UpdateObdxScorePersistencePayload;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxScoreCommand;
import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.stages.exceptions.StageExpiredException;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;

import java.math.BigDecimal;
import java.util.List;

public class UpdateObdxScoreServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort;
    private final GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort;
    private final UpdateObdxScorePersistencePort updateObdxScorePersistencePort;

    public UpdateObdxScoreServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                      GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort,
                                      GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort,
                                      UpdateObdxScorePersistencePort updateObdxScorePersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.getObdxEventCollectorPersistencePort = getObdxEventCollectorPersistencePort;
        this.getObdxExerciseAllowedValuesPort = getObdxExerciseAllowedValuesPort;
        this.updateObdxScorePersistencePort = updateObdxScorePersistencePort;
    }

    public void updateScore(String eventId, UpdateObdxScoreCommand command, String userEmail) {
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(eventId);
        if (competitionId == null) throw new EventNotFoundException();
        Competition competition = getCompetitionPersistencePort.getCompetition(competitionId);
        Event event = CompetitionNavigator.findEvent(competition, eventId);
        assertEventValidations(event);
        Stage stage = CompetitionNavigator.findStageOfEvent(competition, eventId);
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
