package com.k9x.application.events.obdx.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxExerciseAllowedValuesPort;
import com.k9x.application.events.obdx.exceptions.ObdxScoreNotAllowedException;
import com.k9x.application.events.obdx.exceptions.ObdxUserNotCollectorException;
import com.k9x.application.events.obdx.port.GetObdxEventCollectorPersistencePort;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxScoreCommand;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.competitions.commands.ScoreUpdateData;
import com.k9x.domain.events.exceptions.EventNotFoundException;

import java.math.BigDecimal;
import java.util.List;

public class UpdateObdxScoreServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort;
    private final GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    public UpdateObdxScoreServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                      GetObdxEventCollectorPersistencePort getObdxEventCollectorPersistencePort,
                                      GetObdxExerciseAllowedValuesPort getObdxExerciseAllowedValuesPort,
                                      SaveCompetitionPersistencePort saveCompetitionPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.getObdxEventCollectorPersistencePort = getObdxEventCollectorPersistencePort;
        this.getObdxExerciseAllowedValuesPort = getObdxExerciseAllowedValuesPort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
    }

    public void updateScore(String eventId, UpdateObdxScoreCommand command, String userEmail) {
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(eventId);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        assertUserIsCollector(eventId, command.judgeId(), userEmail);
        assertScoreAllowed(command.exerciseId(), command.score());
        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));
        competition.updateScore(eventId,
                new ScoreUpdateData(command.judgeId(), command.exerciseId(), command.dogId(), command.score()),
                DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }

    private void assertUserIsCollector(String eventId, String judgeId, String userEmail) {
        String collectorId = getObdxEventCollectorPersistencePort.getCollectorId(eventId, judgeId);
        if (collectorId == null || !collectorId.equals(userEmail)) {
            throw new ObdxUserNotCollectorException();
        }
    }

    private void assertScoreAllowed(String exerciseId, BigDecimal score) {
        List<BigDecimal> allowedValues = getObdxExerciseAllowedValuesPort.getAllowedValues(exerciseId);
        if (score == null || allowedValues.stream().noneMatch(v -> v.compareTo(score) == 0)) {
            throw new ObdxScoreNotAllowedException();
        }
    }
}
