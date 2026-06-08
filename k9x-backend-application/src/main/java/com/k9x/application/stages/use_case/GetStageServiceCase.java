package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.application.stages.exceptions.StageHasNoEventsException;
import com.k9x.application.stages.exceptions.StageNotFoundException;
import com.k9x.application.stages.use_case.dto.FetchStageDetailCompetitorDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailEventDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.DisciplineConfigurationMalformedException;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class GetStageServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;

    public GetStageServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                               GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.getObdxFederationsConfigurationsPort = getObdxFederationsConfigurationsPort;
    }

    public FetchStageDetailDTO getStage(String id) {
        String competitionId = getCompetitionPersistencePort.competitionIdByStage(id);
        if (competitionId == null) {
            throw new StageNotFoundException();
        }
        Competition competition = getCompetitionPersistencePort.getCompetition(competitionId);
        Stage stage = CompetitionNavigator.findStage(competition, id);

        if (stage == null) {
            throw new StageNotFoundException();
        }
        if (stage.deletedAt() != null) {
            throw new StageAlreadyDeletedException();
        }

        var events = stage.events() == null ? java.util.List.<Event>of()
                : stage.events().stream().filter(e -> e.deletedAt() == null).toList();
        if (events.isEmpty()) {
            throw new StageHasNoEventsException();
        }

        Map<String, String> configNameById = buildConfigNameMap();
        long now = DateUtils.nowUtcMillis();
        return new FetchStageDetailDTO(
                stage.id(), stage.name(), stage.dateFrom(), stage.dateTo(),
                competition.address(), competition.organizerName(), null,
                events.stream()
                        .map(e -> new FetchStageDetailEventDTO(
                                e.id(), e.name(), e.discipline(), e.configurationId(),
                                configNameById.getOrDefault(e.configurationId(), e.configurationId()),
                                e.competitors() == null ? java.util.List.of()
                                        : e.competitors().stream()
                                        .map(c -> new FetchStageDetailCompetitorDTO(
                                                c.dogId(), c.dogName(), c.owner(),
                                                c.country(), c.team(), c.breed()))
                                        .toList(),
                                e.status().name(),
                                e.enrollmentOpened(now)))
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
