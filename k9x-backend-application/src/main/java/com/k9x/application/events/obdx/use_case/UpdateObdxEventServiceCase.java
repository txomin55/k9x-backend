package com.k9x.application.events.obdx.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.events.exceptions.EventCategoryNotAllowedException;
import com.k9x.application.events.exceptions.EventCategoryRequiredException;
import com.k9x.application.events.exceptions.EventConfigurationIdRequiredException;
import com.k9x.application.events.obdx.exceptions.ObdxCollectorNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxDuplicateDogException;
import com.k9x.application.events.obdx.exceptions.ObdxDuplicateExerciseException;
import com.k9x.application.events.obdx.exceptions.ObdxDuplicateJudgeException;
import com.k9x.application.events.obdx.exceptions.ObdxExerciseJudgeNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxExerciseJudgeRequiredException;
import com.k9x.domain.disciplines.obdx.exceptions.ObdxNotEnoughJudgesException;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxEventCommand;
import com.k9x.domain.disciplines.obdx.ObdxConfigurationsRankThresholds;
import com.k9x.domain.disciplines.obdx.ObdxEventCategory;
import com.k9x.domain.disciplines.obdx.ObdxEventRank;
import com.k9x.domain.disciplines.obdx.ObdxScoreAveraging;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.competitions.commands.ObdxCompetitorItem;
import com.k9x.domain.competitions.commands.ObdxEventUpdateData;
import com.k9x.domain.competitions.commands.ObdxExerciseItem;
import com.k9x.domain.competitions.commands.ObdxJudgeItem;
import com.k9x.application.utils.auth.AuthAssertions;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.domain.shared.UtcDates;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateObdxEventServiceCase implements TransactionalUseCase {

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
        assertCategory(command.configurationId(), command.category());
        assertNoDuplicateJudges(command);
        assertNoDuplicateExercises(command);
        assertNoDuplicateDogs(command);
        assertExerciseJudgesExist(command);
        assertEnoughJudgesForMidAvg(command);

        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(id);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        assertCollectorsExist(command);
        Map<String, Dog> competitorDogs = fetchCompetitorDogs(command);
        assertBihAllowedForSex(command, competitorDogs);

        CompetitionSnapshot snapshot = getCompetitionPersistencePort.getCompetition(competitionId);
        CompetitionAggregate competition = CompetitionAggregate.of(snapshot);
        Integer rankScore = ObdxEventRank.eventScore(command.configurationId(), command.competitors().size(),
                command.category());
        competition.updateObdxEventInfo(id, toUpdateData(command, rankScore), userId, DateUtils.nowUtcMillis());
        saveCompetitionPersistencePort.save(competition);
    }

    private ObdxEventUpdateData toUpdateData(UpdateObdxEventCommand command, Integer rankScore) {
        return new ObdxEventUpdateData(
                command.name(),
                command.configurationId(),
                command.scoreCalculation(),
                command.enrollmentDeadline() == null ? null : UtcDates.endOfUtcDay(command.enrollmentDeadline()),
                command.competitors().stream()
                        .map(c -> new ObdxCompetitorItem(c.dogIdentification(), c.order().shortValue(),
                                c.competitorNumber() == null ? null : c.competitorNumber().shortValue(),
                                c.bih(), c.primer(), c.reserve()))
                        .toList(),
                command.exercises().stream()
                        .map(e -> new ObdxExerciseItem(e.exerciseId(), e.order().shortValue(),
                                e.tags() == null ? new String[0] : e.tags().toArray(String[]::new),
                                e.judgeIds() == null ? new String[0] : e.judgeIds().toArray(String[]::new)))
                        .toList(),
                command.judges().stream()
                        .map(j -> new ObdxJudgeItem(j.judgeId(), j.collectorEmail(), j.mainJudge()))
                        .toList(),
                command.awards(),
                rankScore,
                command.commissioner(),
                command.category());
    }

    private Map<String, Dog> fetchCompetitorDogs(UpdateObdxEventCommand command) {
        Map<String, Dog> dogs = new LinkedHashMap<>();
        command.competitors().forEach(c -> dogs.put(c.dogIdentification(), getDogPersistencePort.getDog(c.dogIdentification())));
        return dogs;
    }

    private void assertConfigurationId(String configurationId) {
        if (configurationId == null || configurationId.isBlank()) {
            throw new EventConfigurationIdRequiredException();
        }
    }

    /**
     * The category drives the event's rank score, so it is mandatory and must be one the configuration admits:
     * only the grade hosting the world championship accepts the {@code WC_*} rounds. Configurations with no
     * rank band declared score {@code null} and accept any category.
     */
    private void assertCategory(String configurationId, ObdxEventCategory category) {
        if (category == null) {
            throw new EventCategoryRequiredException();
        }
        ObdxConfigurationsRankThresholds band = ObdxConfigurationsRankThresholds.fromConfigurationId(configurationId);
        if (band != null && !band.allows(category)) {
            throw new EventCategoryNotAllowedException();
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

    private void assertExerciseJudgesExist(UpdateObdxEventCommand command) {
        Set<String> eventJudgeIds = command.judges().stream()
                .map(UpdateObdxEventCommand.JudgeCommand::judgeId)
                .collect(Collectors.toSet());
        command.exercises().forEach(e -> {
            if (e.judgeIds() == null || e.judgeIds().isEmpty()) {
                throw new ObdxExerciseJudgeRequiredException(e.exerciseId());
            }
            e.judgeIds().forEach(judgeId -> {
                if (!eventJudgeIds.contains(judgeId)) {
                    throw new ObdxExerciseJudgeNotFoundException(judgeId);
                }
            });
        });
    }

    /**
     * MID_AVG discards the single highest and lowest score per exercise, so every exercise needs enough
     * assigned judges for that to be meaningful (see {@link ObdxScoreAveraging#hasEnoughJudges}) — otherwise it
     * degenerates into (near-)AVG.
     */
    private void assertEnoughJudgesForMidAvg(UpdateObdxEventCommand command) {
        command.exercises().forEach(e -> {
            if (e.judgeIds() != null
                    && !ObdxScoreAveraging.hasEnoughJudges(command.scoreCalculation(), e.judgeIds().size())) {
                throw new ObdxNotEnoughJudgesException();
            }
        });
    }

    private void assertNoDuplicateDogs(UpdateObdxEventCommand command) {
        Set<String> seen = new HashSet<>();
        command.competitors().forEach(c -> {
            if (!seen.add(c.dogIdentification())) {
                throw new ObdxDuplicateDogException();
            }
        });
    }

    private void assertBihAllowedForSex(UpdateObdxEventCommand command, Map<String, Dog> competitorDogs) {
        command.competitors().forEach(c ->
                BihGuards.assertBihAllowedForSex(c.bih(), competitorDogs.get(c.dogIdentification())));
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
