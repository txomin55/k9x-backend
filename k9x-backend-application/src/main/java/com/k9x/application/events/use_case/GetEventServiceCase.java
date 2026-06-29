package com.k9x.application.events.use_case;

import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.disciplines.use_case.dto.ConfigurationsDTO;
import com.k9x.application.disciplines.use_case.dto.ExerciseDTO;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventJudgeDTO;
import com.k9x.application.events.use_case.dto.FetchEventConfigurationDTO;
import com.k9x.application.events.use_case.dto.FetchEventDetailDTO;
import com.k9x.application.events.use_case.dto.FetchEventExerciseDTO;
import com.k9x.domain.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.status.EventCompetitorStatus;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class GetEventServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;

    public GetEventServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                               GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.getObdxFederationsConfigurationsPort = getObdxFederationsConfigurationsPort;
    }

    public FetchEventDetailDTO getEvent(String id, String userId, boolean organizer) {
        assertOrganizer(organizer);
        String competitionId = getCompetitionPersistencePort.competitionIdByEvent(id);
        if (competitionId == null) {
            throw new EventNotFoundException();
        }
        CompetitionSnapshot competition = getCompetitionPersistencePort.getCompetition(competitionId);
        EventSnapshot event = CompetitionNavigator.findEvent(competition, id);
        assertEventExists(event);
        StageSnapshot stage = CompetitionNavigator.findStageOfEvent(competition, id);
        assertStageValidations(stage, userId);

        Discipline discipline = Discipline.valueOf(event.discipline().toUpperCase(Locale.ROOT));
        if (discipline != Discipline.OBDX) {
            return new FetchEventDetailDTO(null, List.of(), List.of(), List.of(), null);
        }
        return buildObdxDetail(event, stage);
    }

    private FetchEventDetailDTO buildObdxDetail(EventSnapshot event, StageSnapshot stage) {
        FetchObdxEventDTO obdx = new FetchObdxEventDTO(event.id(), event.name(), stage.id(), stage.name(),
                event.discipline(), event.status().name(), event.enrollmentDeadline());

        List<FetchObdxEventCompetitorDTO> competitors = event.competitors().stream()
                .map(c -> new FetchObdxEventCompetitorDTO(c.dogId(), c.dogName(), c.identity(), c.breed(),
                        c.owner(), c.team(), c.country(), c.position(), c.verified(),
                        EventCompetitorStatus.of(c.notCompeting()).name()))
                .toList();

        List<FetchObdxEventJudgeDTO> judges = event.judges().stream()
                .map(j -> new FetchObdxEventJudgeDTO(j.judgeId(), j.judgeName(), j.collectorEmail()))
                .toList();

        ConfigurationsDTO federation = resolveFederationConfiguration(event.configurationId());
        ConfigurationDTO configuration = federation == null ? null : federation.configurations().stream()
                .filter(c -> c.id().equals(event.configurationId()))
                .findFirst().orElse(null);

        Map<String, String> exerciseNames = configuration == null ? Map.of() : configuration.exercises().stream()
                .collect(Collectors.toMap(ExerciseDTO::id, ExerciseDTO::name, (a, _) -> a));

        List<FetchEventExerciseDTO> exercises = event.exercises().stream()
                .map(e -> new FetchEventExerciseDTO(e.exerciseId(), exerciseNames.get(e.exerciseId()),
                        e.position() == null ? null : e.position().intValue(), e.tags()))
                .toList();

        FetchEventConfigurationDTO configurationDetail = configuration == null ? null
                : new FetchEventConfigurationDTO(configuration.id(), configuration.name(), federation.info());

        return new FetchEventDetailDTO(obdx, competitors, exercises, judges, configurationDetail);
    }

    private ConfigurationsDTO resolveFederationConfiguration(String configurationId) {
        if (configurationId == null) {
            return null;
        }
        List<ConfigurationsDTO> federations;
        try {
            federations = getObdxFederationsConfigurationsPort.getConfigurations();
        } catch (IOException e) {
            throw new DisciplineConfigurationMalformedException();
        }
        return federations.stream()
                .filter(f -> f.configurations().stream().anyMatch(c -> c.id().equals(configurationId)))
                .findFirst().orElse(null);
    }

    private void assertOrganizer(boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }
    }

    private void assertEventExists(EventSnapshot event) {
        if (event == null) {
            throw new EventNotFoundException();
        }
    }

    private void assertStageValidations(StageSnapshot stage, String userId) {
        if (stage.deletedAt() != null) {
            throw new StageAlreadyDeletedException();
        }
        if (!stage.creator().equals(userId)) {
            throw new UnauthorizedResourceException();
        }
    }
}
