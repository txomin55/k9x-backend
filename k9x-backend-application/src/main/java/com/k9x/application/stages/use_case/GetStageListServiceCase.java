package com.k9x.application.stages.use_case;

import com.k9x.application.notifications.port.GetStageNotificationsPersistencePort;
import com.k9x.application.rankings.port.GetRankedEventIdsPersistencePort;
import com.k9x.application.notifications.use_case.dto.StageNotificationDTO;
import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageListDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListEventDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListRowDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListRowEventDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.application.utils.stages.StageProximity;
import com.k9x.domain.disciplines.obdx.ObdxRank;
import com.k9x.domain.events.status.EventLifecycle;
import com.k9x.domain.events.status.EventStatus;
import com.k9x.domain.shared.UtcDates;
import com.k9x.domain.stages.status.StageLifecycle;
import com.k9x.domain.stages.status.StageStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The public stage list, read through its own query projection ({@link FetchStageListRowDTO}) rather than the
 * competition aggregate: the date range is filtered in the database and the events arrive with the facts the
 * lifecycle rules need, so {@link EventLifecycle} and {@link StageLifecycle} resolve the statuses without a
 * single score in memory.
 */
public class GetStageListServiceCase {

    private final GetStageListPersistencePort getStageListPersistencePort;
    private final GetStageNotificationsPersistencePort getStageNotificationsPersistencePort;

    private final GetRankedEventIdsPersistencePort getRankedEventIdsPersistencePort;

    public GetStageListServiceCase(GetStageListPersistencePort getStageListPersistencePort,
                                   GetStageNotificationsPersistencePort getStageNotificationsPersistencePort,
                                   GetRankedEventIdsPersistencePort getRankedEventIdsPersistencePort) {
        this.getStageListPersistencePort = getStageListPersistencePort;
        this.getStageNotificationsPersistencePort = getStageNotificationsPersistencePort;
        this.getRankedEventIdsPersistencePort = getRankedEventIdsPersistencePort;
    }

    public List<FetchStageListDTO> getStages(Long from, Long to) {
        long now = DateUtils.nowUtcMillis();
        List<FetchStageListRowDTO> stages = getStageListPersistencePort
                .getStages(from, to, UtcDates.startOfUtcDay(now)).stream()
                .sorted((a, b) -> StageProximity.compareByProximity(a.dateFrom(), b.dateFrom(), now))
                .toList();
        // One query for every stage in the response: announcements are read outside the aggregate, and doing
        // it per stage would be an N+1.
        Map<String, List<StageNotificationDTO>> notificationsByStage = getStageNotificationsPersistencePort
                .getByStageIds(stages.stream().map(FetchStageListRowDTO::id).toList());
        // Also one query for the whole response, so the flag costs the same no matter how many stages.
        Set<String> rankedEventIds = getRankedEventIdsPersistencePort.getRankedEventIds();
        return stages.stream()
                .map(stage -> toStageDto(stage, now, notificationsByStage, rankedEventIds))
                .toList();
    }

    private FetchStageListDTO toStageDto(FetchStageListRowDTO stage, long now,
                                        Map<String, List<StageNotificationDTO>> notificationsByStage,
                                        Set<String> rankedEventIds) {
        StageStatus stageStatus = StageLifecycle.status(null, now, stage.dateFrom(), stage.dateTo(),
                () -> stage.events().stream().map(event -> eventStatus(event, stage, now)).toList(),
                () -> stage.events().stream().anyMatch(FetchStageListRowEventDTO::hasAnyScore));
        List<FetchStageListRowEventDTO> activeEvents = stage.events().stream()
                .filter(event -> event.deletedAt() == null)
                .toList();
        return new FetchStageListDTO(
                stage.id(), stage.name(), stage.competitionName(), stage.country(),
                stage.address(), stage.coordAlt(), stage.coordLong(),
                stage.dateFrom(), stage.dateTo(),
                stage.organizer(),
                activeEvents.stream()
                        .map(event -> new FetchStageListEventDTO(
                                event.id(), event.name(), event.disciplineId(), event.competitorCount(),
                                eventStatus(event, stage, now).name(),
                                StageLifecycle.enrollmentOpened(stageStatus, event.enrollmentDeadline(), now),
                                event.enrollmentDeadline(), event.awards(),
                                event.rankScore() == null ? null : ObdxRank.labelFromScore(event.rankScore())))
                        .toList(),
                stageStatus.name(),
                notificationsByStage.getOrDefault(stage.id(), List.of()),
                activeEvents.stream().anyMatch(event -> rankedEventIds.contains(event.id())),
                stage.extraction());
    }

    private static EventStatus eventStatus(FetchStageListRowEventDTO event, FetchStageListRowDTO stage, long now) {
        return EventLifecycle.status(event.deletedAt(), now, stage.dateTo(),
                event::allCompetitorsSettled, event::hasAnyScore);
    }
}
