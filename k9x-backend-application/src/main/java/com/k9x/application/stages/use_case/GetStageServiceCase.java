package com.k9x.application.stages.use_case;

import com.k9x.application.competitions.CompetitionNavigator;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.notifications.port.GetStageNotificationsPersistencePort;
import com.k9x.domain.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.domain.stages.exceptions.StageNotFoundException;
import com.k9x.application.stages.use_case.dto.FetchStageDetailCompetitorDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailEventDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class GetStageServiceCase {

    private final GetCompetitionPersistencePort getCompetitionPersistencePort;
    private final GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;
    private final GetStageNotificationsPersistencePort getStageNotificationsPersistencePort;

    public GetStageServiceCase(GetCompetitionPersistencePort getCompetitionPersistencePort,
                               GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort,
                               GetStageNotificationsPersistencePort getStageNotificationsPersistencePort) {
        this.getCompetitionPersistencePort = getCompetitionPersistencePort;
        this.getObdxFederationsConfigurationsPort = getObdxFederationsConfigurationsPort;
        this.getStageNotificationsPersistencePort = getStageNotificationsPersistencePort;
    }

    public FetchStageDetailDTO getStage(String id) {
        String competitionId = getCompetitionPersistencePort.competitionIdByStage(id);
        if (competitionId == null) {
            throw new StageNotFoundException();
        }
        CompetitionSnapshot competition = getCompetitionPersistencePort.getCompetition(competitionId);
        StageSnapshot stage = CompetitionNavigator.findStage(competition, id);

        if (stage == null) {
            throw new StageNotFoundException();
        }
        if (stage.deletedAt() != null) {
            throw new StageAlreadyDeletedException();
        }

        var events = stage.events() == null ? java.util.List.<EventSnapshot>of()
                : stage.events().stream().filter(e -> e.deletedAt() == null).toList();

        Map<String, String> configNameById = buildConfigNameMap();
        long now = DateUtils.nowUtcMillis();
        return new FetchStageDetailDTO(
                stage.id(), stage.name(), competition.name(), stage.dateFrom(), stage.dateTo(),
                competition.address(), competition.organizerName(), stage.status(now).name(), null,
                events.stream()
                        .map(e -> new FetchStageDetailEventDTO(
                                e.id(), e.name(), e.discipline(), e.configurationId(),
                                configNameById.getOrDefault(e.configurationId(), e.configurationId()),
                                e.competitors() == null ? java.util.List.of()
                                        : e.competitors().stream()
                                        .map(c -> new FetchStageDetailCompetitorDTO(
                                                c.dogIdentification(), c.dogName(), c.owner(), c.handler(),
                                                c.country(), c.team(), c.breed(),
                                                c.verified() != null && c.verified()))
                                        .toList(),
                                e.status(now, stage.dateTo()).name(),
                                stage.enrollmentOpened(e, now),
                                e.enrollmentDeadline(), e.awards(), e.rank()))
                        .toList(),
                // Announcements live outside the competition aggregate, so they are read through their own port.
                getStageNotificationsPersistencePort.getByStageIds(java.util.List.of(id))
                        .getOrDefault(id, java.util.List.of()));
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
