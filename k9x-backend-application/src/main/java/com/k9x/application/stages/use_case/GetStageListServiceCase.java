package com.k9x.application.stages.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageListDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListEventDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;

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
        long now = DateUtils.nowUtcMillis();
        return getStageListPersistencePort.getStages().stream()
                .map(stage -> {
                    // Global list read-model: events (and their scores) are not hydrated here, so the
                    // status is computed from the stage dates only. STARTED (driven by event scores) is
                    // therefore not surfaced in this list; the detailed/root-aggregate views are exact.
                    String status = new StageSnapshot(stage.id(), stage.name(), null, null,
                            stage.dateFrom(), stage.dateTo(), 0L, 0L, null, List.of())
                            .status(now).name();
                    return new FetchStageListDTO(
                            stage.id(), stage.name(), stage.description(), stage.country(),
                            stage.address(), stage.coordAlt(), stage.coordLong(),
                            stage.dateFrom(), stage.dateTo(),
                            stage.organizer(),
                            stage.events().stream()
                                    .map(e -> new FetchStageListEventDTO(
                                            e.id(), e.name(), e.configurationId(),
                                            configNameById.getOrDefault(e.configurationId(), e.configurationId()),
                                            e.competitorCount(),
                                            status))
                                    .toList(),
                            status);
                })
                .toList();
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
}
