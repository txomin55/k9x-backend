package com.k9x.application.stages.use_case;

import com.k9x.application.notifications.port.GetStageNotificationsPersistencePort;
import com.k9x.application.notifications.use_case.dto.StageNotificationDTO;
import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageListDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListEventDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.application.utils.stages.StageProximity;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;

import java.util.List;
import java.util.Map;

public class GetStageListServiceCase {

    private final GetStageListPersistencePort getStageListPersistencePort;
    private final GetStageNotificationsPersistencePort getStageNotificationsPersistencePort;

    public GetStageListServiceCase(GetStageListPersistencePort getStageListPersistencePort,
                                   GetStageNotificationsPersistencePort getStageNotificationsPersistencePort) {
        this.getStageListPersistencePort = getStageListPersistencePort;
        this.getStageNotificationsPersistencePort = getStageNotificationsPersistencePort;
    }

    public List<FetchStageListDTO> getStages(Long from, Long to) {
        long now = DateUtils.nowUtcMillis();
        List<CompetitionStage> stages = getStageListPersistencePort.getCompetitions().stream()
                .flatMap(competition -> competition.stages().stream()
                        .filter(stage -> stage.deletedAt() == null)
                        .filter(stage -> withinRange(stage.dateFrom(), from, to))
                        .map(stage -> new CompetitionStage(competition, stage)))
                .sorted((a, b) -> StageProximity.compareByProximity(
                        a.stage().dateFrom(), b.stage().dateFrom(), now))
                .toList();
        // One query for every stage in the response: announcements are read outside the aggregate, and doing
        // it per stage would be an N+1.
        Map<String, List<StageNotificationDTO>> notificationsByStage = getStageNotificationsPersistencePort
                .getByStageIds(stages.stream().map(cs -> cs.stage().id()).toList());
        return stages.stream()
                .map(cs -> toStageDto(cs.competition(), cs.stage(), now, notificationsByStage))
                .toList();
    }

    private boolean withinRange(long dateFrom, Long from, Long to) {
        return (from == null || dateFrom >= from) && (to == null || dateFrom <= to);
    }

    private FetchStageListDTO toStageDto(CompetitionSnapshot competition, StageSnapshot stage, long now,
                                        Map<String, List<StageNotificationDTO>> notificationsByStage) {
        // The hydrated events carry their scores, so status() resolves the exact lifecycle here: the stage
        // is STARTED once any of its events holds a score, otherwise it falls back to the date-driven state.
        return new FetchStageListDTO(
                stage.id(), stage.name(), competition.name(), competition.country(),
                competition.address(), competition.coordAlt(), competition.coordLong(),
                stage.dateFrom(), stage.dateTo(),
                competition.organizerName(),
                stage.events().stream()
                        .filter(event -> event.deletedAt() == null)
                        .map(event -> new FetchStageListEventDTO(
                                event.id(), event.name(), event.discipline(),
                                event.competitors() == null ? 0 : event.competitors().size(),
                                event.status(now, stage.dateTo()).name(),
                                stage.enrollmentOpened(event, now),
                                event.enrollmentDeadline(), event.awards(), event.rank()))
                        .toList(),
                stage.status(now).name(),
                notificationsByStage.getOrDefault(stage.id(), List.of()));
    }

    private record CompetitionStage(CompetitionSnapshot competition, StageSnapshot stage) {
    }
}
