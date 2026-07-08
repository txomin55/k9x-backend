package com.k9x.application.stages.use_case;

import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageListDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListEventDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.shared.UtcDates;
import com.k9x.domain.stages.aggregates.StageSnapshot;

import java.util.Comparator;
import java.util.List;

public class GetStageListServiceCase {

    private final GetStageListPersistencePort getStageListPersistencePort;

    public GetStageListServiceCase(GetStageListPersistencePort getStageListPersistencePort) {
        this.getStageListPersistencePort = getStageListPersistencePort;
    }

    public List<FetchStageListDTO> getStages() {
        long now = DateUtils.nowUtcMillis();
        return getStageListPersistencePort.getCompetitions().stream()
                .flatMap(competition -> competition.stages().stream()
                        .filter(stage -> stage.deletedAt() == null)
                        .map(stage -> new CompetitionStage(competition, stage)))
                .sorted(Comparator
                        .comparing((CompetitionStage cs) -> UtcDates.isBeforeUtcDay(cs.stage().dateFrom(), now))
                        .thenComparingLong(cs -> cs.stage().dateFrom()))
                .map(cs -> toStageDto(cs.competition(), cs.stage(), now))
                .toList();
    }

    private FetchStageListDTO toStageDto(CompetitionSnapshot competition, StageSnapshot stage, long now) {
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
                stage.status(now).name());
    }

    private record CompetitionStage(CompetitionSnapshot competition, StageSnapshot stage) {
    }
}
