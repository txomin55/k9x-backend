package com.k9x.application.events.obdx.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.events.exceptions.EventConfigurationIdRequiredException;
import com.k9x.application.events.obdx.exceptions.ObdxCollectorNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxDuplicateDogException;
import com.k9x.application.events.obdx.exceptions.ObdxDuplicateExerciseException;
import com.k9x.application.events.obdx.exceptions.ObdxDuplicateJudgeException;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxEventCommand;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.competitions.commands.ObdxCompetitorItem;
import com.k9x.domain.competitions.commands.ObdxEventUpdateData;
import com.k9x.domain.competitions.commands.ObdxExerciseItem;
import com.k9x.domain.competitions.commands.ObdxJudgeItem;
import com.k9x.application.utils.auth.AuthAssertions;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.shared.UtcDates;

import java.util.HashSet;
import java.util.Set;

public class UpdateObdxEventServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final SaveCompetitionPersistencePort saveCompetitionPersistencePort;
    private final GetUserInfoPersistencePort getUserInfoPersistencePort;
    private final GetDogPersistencePort getDogPersistencePort;

    public UpdateObdxEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                                      SaveCompetitionPersistencePort saveCompetitionPersistencePort,
                                      GetUserInfoPersistencePort getUserInfoPersistencePort,
                                      GetDogPersistencePort getDogPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.saveCompetitionPersistencePort = saveCompetitionPersistencePort;
        this.getUserInfoPersistencePort = getUserInfoPersistencePort;
        this.getDogPersistencePort = getDogPersistencePort;
    }

    public void updateEvent(String id, UpdateObdxEventCommand command, String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);
        assertConfigurationId(command.configurationId());
        assertNoDuplicateJudges(command);
        assertNoDuplicateExercises(command);
        assertNoDuplicateDogs(command);

        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(id);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        assertCollectorsExist(command);
        assertBihAllowedForSex(command);

        CompetitionAggregate competition =
                CompetitionAggregate.of(getCompetitionPersistencePort.getCompetition(competitionId));
        competition.updateObdxEventInfo(id, toUpdateData(command), userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }

    private ObdxEventUpdateData toUpdateData(UpdateObdxEventCommand command) {
        return new ObdxEventUpdateData(
                command.name(),
                command.configurationId(),
                command.scoreCalculation(),
                command.enrollmentDeadline() == null ? null : UtcDates.endOfUtcDay(command.enrollmentDeadline()),
                command.competitors().stream()
                        .map(c -> new ObdxCompetitorItem(c.dogId(), c.order().shortValue(), c.bih()))
                        .toList(),
                command.exercises().stream()
                        .map(e -> new ObdxExerciseItem(e.exerciseId(), e.order().shortValue(),
                                e.tags() == null ? new String[0] : e.tags().toArray(String[]::new)))
                        .toList(),
                command.judges().stream()
                        .map(j -> new ObdxJudgeItem(j.judgeId(), j.collectorEmail()))
                        .toList(),
                command.awards());
    }

    private void assertConfigurationId(String configurationId) {
        if (configurationId == null || configurationId.isBlank()) {
            throw new EventConfigurationIdRequiredException();
        }
    }

    private void assertNoDuplicateJudges(UpdateObdxEventCommand command) {
        Set<String> seen = new HashSet<>();
        command.judges().forEach(j -> {
            if (!seen.add(j.judgeId())) {
                throw new ObdxDuplicateJudgeException();
            }
        });
    }

    private void assertNoDuplicateExercises(UpdateObdxEventCommand command) {
        Set<String> seen = new HashSet<>();
        command.exercises().forEach(e -> {
            if (!seen.add(e.exerciseId())) {
                throw new ObdxDuplicateExerciseException();
            }
        });
    }

    private void assertNoDuplicateDogs(UpdateObdxEventCommand command) {
        Set<String> seen = new HashSet<>();
        command.competitors().forEach(c -> {
            if (!seen.add(c.dogId())) {
                throw new ObdxDuplicateDogException();
            }
        });
    }

    private void assertBihAllowedForSex(UpdateObdxEventCommand command) {
        command.competitors().forEach(c ->
                BihGuards.assertBihAllowedForSex(c.bih(), getDogPersistencePort.getDog(c.dogId())));
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
