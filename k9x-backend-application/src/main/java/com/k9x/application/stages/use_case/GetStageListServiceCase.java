package com.k9x.application.stages.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.obdx.use_case.dto.ObdxConfigurationDTO;
import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageListDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListEventDTO;
import com.k9x.domain.aggregates.stages.StageStatus;
import com.k9x.domain.exceptions.DisciplineConfigurationMalformedException;

import java.io.IOException;
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
        return getStageListPersistencePort.getStages().stream()
                .map(stage -> new FetchStageListDTO(
                        stage.id(), stage.name(), stage.description(), stage.country(),
                        stage.address(), stage.coordAlt(), stage.coordLong(),
                        stage.dateFrom(), stage.dateTo(),
                        stage.organizer(),
                        stage.events().stream()
                                .map(e -> new FetchStageListEventDTO(
                                        e.id(), e.name(), e.configurationId(),
                                        configNameById.getOrDefault(e.configurationId(), e.configurationId()),
                                        e.competitorCount(),
                                        StageStatus.OPEN.name()))
                                .toList(),
                        StageStatus.OPEN.name()))
                .toList();
    }

    private Map<String, String> buildConfigNameMap() {
        try {
            return getObdxFederationsConfigurationsPort.getConfigurations().stream()
                    .flatMap(f -> f.configurations().stream())
                    .collect(Collectors.toMap(ObdxConfigurationDTO::id, ObdxConfigurationDTO::name, (a, b) -> a));
        } catch (IOException e) {
            throw new DisciplineConfigurationMalformedException();
        }
    }
}
