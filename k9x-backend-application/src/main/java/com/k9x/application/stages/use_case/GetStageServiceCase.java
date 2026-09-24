package com.k9x.application.stages.use_case;

import com.k9x.application.disciplines.obdx.port.GetObdxFederationsConfigurationsPort;
import com.k9x.application.disciplines.use_case.dto.ConfigurationDTO;
import com.k9x.application.notifications.port.GetStageNotificationsPersistencePort;
import com.k9x.application.stages.port.GetStageDetailPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageDetailDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailEventDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailRowDTO;
import com.k9x.application.stages.use_case.dto.FetchStageDetailRowEventDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.disciplines.exceptions.DisciplineConfigurationMalformedException;
import com.k9x.domain.disciplines.obdx.ObdxRank;
import com.k9x.domain.events.status.EventLifecycle;
import com.k9x.domain.events.status.EventStatus;
import com.k9x.domain.shared.UtcDates;
import com.k9x.domain.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.domain.stages.exceptions.StageNotFoundException;
import com.k9x.domain.stages.status.StageLifecycle;
import com.k9x.domain.stages.status.StageStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The public stage detail, read through its own query projection ({@link FetchStageDetailRowDTO}) rather than the
 * competition aggregate; {@link EventLifecycle} and {@link StageLifecycle} resolve the statuses from the facts it
 * carries.
 */
public class GetStageServiceCase {

    private final GetStageDetailPersistencePort getStageDetailPersistencePort;
    private final GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort;
    private final GetStageNotificationsPersistencePort getStageNotificationsPersistencePort;

    public GetStageServiceCase(GetStageDetailPersistencePort getStageDetailPersistencePort,
                               GetObdxFederationsConfigurationsPort getObdxFederationsConfigurationsPort,
                               GetStageNotificationsPersistencePort getStageNotificationsPersistencePort) {
        this.getStageDetailPersistencePort = getStageDetailPersistencePort;
        this.getObdxFederationsConfigurationsPort = getObdxFederationsConfigurationsPort;
        this.getStageNotificationsPersistencePort = getStageNotificationsPersistencePort;
    }

    public FetchStageDetailDTO getStage(String id) {
        long now = DateUtils.nowUtcMillis();
        FetchStageDetailRowDTO stage = getStageDetailPersistencePort.getStage(id, UtcDates.startOfUtcDay(now))
                .orElseThrow(StageNotFoundException::new);
        if (stage.deletedAt() != null) {
            throw new StageAlreadyDeletedException();
        }

        StageStatus stageStatus = StageLifecycle.status(null, now, stage.dateFrom(), stage.dateTo(),
                () -> stage.events().stream().map(event -> eventStatus(event, stage, now)).toList(),
                () -> stage.events().stream().anyMatch(FetchStageDetailRowEventDTO::hasAnyScore));
        Map<String, String> configNameById = buildConfigNameMap();
        return new FetchStageDetailDTO(
                stage.id(), stage.name(), stage.competitionName(), stage.dateFrom(), stage.dateTo(),
                stage.address(), stage.organizer(), stageStatus.name(), null,
                stage.events().stream()
                        .filter(e -> e.deletedAt() == null)
                        .map(e -> new FetchStageDetailEventDTO(
                                e.id(), e.name(), e.disciplineId(), e.configurationId(),
                                configNameById.getOrDefault(e.configurationId(), e.configurationId()),
                                e.competitors(),
                                eventStatus(e, stage, now).name(),
                                StageLifecycle.enrollmentOpened(stageStatus, e.enrollmentDeadline(), now),
                                e.enrollmentDeadline(), e.awards(),
                                e.rankScore() == null ? null : ObdxRank.labelFromScore(e.rankScore())))
                        .toList(),
                // Announcements are written outside the stage, so they are read through their own port.
                getStageNotificationsPersistencePort.getByStageIds(List.of(id)).getOrDefault(id, List.of()),
                stage.extraction());
    }

    private static EventStatus eventStatus(FetchStageDetailRowEventDTO event, FetchStageDetailRowDTO stage, long now) {
        return EventLifecycle.status(event.deletedAt(), now, stage.dateTo(),
                event::allCompetitorsSettled, event::hasAnyScore);
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
