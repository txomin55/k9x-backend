package com.k9x.application.stages.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageListDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListEventDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetStageListServiceCase {

    private final GetStageListPersistencePort getStageListPersistencePort;
    private final GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;

    public GetStageListServiceCase(GetStageListPersistencePort getStageListPersistencePort,
                                   GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        this.getStageListPersistencePort = getStageListPersistencePort;
        this.getObdxFederationsConfigurationsPort = getObdxFederationsConfigurationsPort;
    }

    public List<FetchStageListDTO> getStages() {
        Map<String, String> configNameById = buildConfigNameMap();
        long now = DateUtils.nowUtcMillis();
        return getStageListPersistencePort.getCompetitions().stream()
                .flatMap(competition -> competition.stages().stream()
                        .filter(stage -> stage.deletedAt() == null)
                        .map(stage -> new CompetitionStage(competition, stage)))
                .sorted(Comparator
                        .comparing((CompetitionStage cs) -> cs.stage().dateFrom() < now)
                        .thenComparingLong(cs -> cs.stage().dateFrom()))
                .map(cs -> toStageDto(cs.competition(), cs.stage(), now, configNameById))
                .toList();
    }

    private FetchStageListDTO toStageDto(CompetitionSnapshot competition, StageSnapshot stage, long now,
                                         Map<String, String> configNameById) {
        // The hydrated events carry their scores, so status() resolves the exact lifecycle here: the stage
        // is STARTED once any of its events holds a score, otherwise it falls back to the date-driven state.
        return new FetchStageListDTO(
                stage.id(), stage.name(), competition.description(), competition.country(),
                competition.address(), competition.coordAlt(), competition.coordLong(),
                stage.dateFrom(), stage.dateTo(),
                competition.organizerName(),
                stage.events().stream()
                        .filter(event -> event.deletedAt() == null)
                        .map(event -> new FetchStageListEventDTO(
                                event.id(), event.name(), event.configurationId(),
                                configNameById.getOrDefault(event.configurationId(), event.configurationId()),
                                event.competitors() == null ? 0 : event.competitors().size(),
                                event.status(now, stage.dateTo()).name()))
                        .toList(),
                stage.status(now).name());
    }

    private Map<String, String> buildConfigNameMap() {
        try {
            return getObdxFederationsConfigurationsPort.getConfigurations().stream()
                    .flatMap(f -> f.configurations().stream())
                    .collect(Collectors.toMap(ConfigurationDTO::id, ConfigurationDTO::name, (a, _) -> a));
        } catch (IOException e) {
            throw new DisciplineConfigurationMalformedException();
        }
    }

    private record CompetitionStage(CompetitionSnapshot competition, StageSnapshot stage) {
    }
}
