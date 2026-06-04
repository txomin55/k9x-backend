package com.k9x.application.stages.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageHasNoEventsException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
import com.k9x.application.stages.port.GetStageDetailPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageDetailDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailEventDTO;
import com.k9x.domain.exceptions.DisciplineConfigurationMalformedException;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class GetStageServiceCase {

    private final GetStageDetailPersistencePort getStageDetailPersistencePort;
    private final GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;

    public GetStageServiceCase(GetStageDetailPersistencePort getStageDetailPersistencePort,
                               GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        this.getStageDetailPersistencePort = getStageDetailPersistencePort;
        this.getObdxFederationsConfigurationsPort = getObdxFederationsConfigurationsPort;
    }

    public FetchStageDetailDTO getStage(String id) {
        FetchStageDetailDTO stage = getStageDetailPersistencePort.getStage(id);

        if (stage == null) {
            throw new StageNotFoundException();
        }
        if (stage.deletedAt() != null) {
            throw new StageAlreadyDeletedException();
        }
        if (stage.events().isEmpty()) {
            throw new StageHasNoEventsException();
        }

        Map<String, String> configNameById = buildConfigNameMap();
        return new FetchStageDetailDTO(
                stage.id(), stage.name(), stage.dateFrom(), stage.dateTo(),
                stage.address(), stage.organizer(), null,
                stage.events().stream()
                        .map(e -> new FetchStageDetailEventDTO(
                                e.id(), e.name(), e.disciplineId(), e.configurationId(),
                                configNameById.getOrDefault(e.configurationId(), e.configurationId()),
                                e.competitors()))
                        .toList());
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
