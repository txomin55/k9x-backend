package com.k9x.application.events.obdx.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.events.exceptions.EventConfigurationIdRequiredException;
import com.k9x.application.events.obdx.exceptions.ObdxCollectorNotFoundException;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxEventCommand;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.competitions.commands.ObdxCompetitorItem;
import com.k9x.domain.competitions.commands.ObdxEventUpdateData;
import com.k9x.domain.competitions.commands.ObdxExerciseItem;
import com.k9x.domain.competitions.commands.ObdxJudgeItem;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.shared.SupportUser;

public class UpdateObdxEventServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;
    private final GetObdxClassificationConfigPort getObdxClassificationConfigPort;
    private final GetUserInfoPersistencePort getUserInfoPersistencePort;

    public UpdateObdxEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                      SaveCompetitionPersistencePort saveCompetitionPersistencePort,
                                      GetObdxClassificationConfigPort getObdxClassificationConfigPort,
                                      GetUserInfoPersistencePort getUserInfoPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
        this.getObdxClassificationConfigPort = getObdxClassificationConfigPort;
        this.getUserInfoPersistencePort = getUserInfoPersistencePort;
    }

    public void updateEvent(String id, UpdateObdxEventCommand command, String userId, boolean organizer) {
        assertOrganizer(organizer, userId);
        assertConfigurationId(command.configurationId());

        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(id);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        assertCollectorsExist(command);

        ObdxAvgMethod scoreCalculation = getObdxClassificationConfigPort
                .getConfig(command.configurationId())
                .cacheEvictStrategy()
                .getAvgMethod();

        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));
        competition.updateObdxEventInfo(id, toUpdateData(command, scoreCalculation), userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }

    private ObdxEventUpdateData toUpdateData(UpdateObdxEventCommand command, ObdxAvgMethod scoreCalculation) {
        return new ObdxEventUpdateData(
                command.name(),
                command.configurationId(),
                scoreCalculation,
                command.enrollmentDeadline(),
                command.competitors().stream()
                        .map(c -> new ObdxCompetitorItem(c.dogId(), c.order().shortValue()))
                        .toList(),
                command.exercises().stream()
                        .map(e -> new ObdxExerciseItem(e.exerciseId(), e.order().shortValue(),
                                e.tags() == null ? new String[0] : e.tags().toArray(String[]::new)))
                        .toList(),
                command.judges().stream()
                        .map(j -> new ObdxJudgeItem(j.judgeId(), j.collectorEmail()))
                        .toList());
    }

    private void assertOrganizer(boolean organizer, String userId) {
        if (!organizer && !SupportUser.is(userId)) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertConfigurationId(String configurationId) {
        if (configurationId == null || configurationId.isBlank()) {
            throw new EventConfigurationIdRequiredException();
        }
    }

    private void assertCollectorsExist(UpdateObdxEventCommand command) {
        command.judges().stream()
                .map(UpdateObdxEventCommand.JudgeCommand::collectorEmail)
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .forEach(email -> {
                    if (getUserInfoPersistencePort.findById(email) == null) {
                        throw new ObdxCollectorNotFoundException(email);
                    }
                });
    }
}
