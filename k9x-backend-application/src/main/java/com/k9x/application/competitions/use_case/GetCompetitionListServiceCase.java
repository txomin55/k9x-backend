package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.GetCompetitionListPersistencePort;
import com.k9x.application.competitions.use_case.dto.FetchCompetitionDTO;
import com.k9x.application.competitions.use_case.dto.FetchEventDTO;
import com.k9x.application.competitions.use_case.dto.FetchStageDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.application.utils.stages.StageProximity;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.application.utils.auth.AuthAssertions;

import java.util.List;

public class GetCompetitionListServiceCase {

    private final GetCompetitionListPersistencePort getCompetitionListPersistencePort;

    public GetCompetitionListServiceCase(GetCompetitionListPersistencePort getCompetitionListPersistencePort) {
        this.getCompetitionListPersistencePort = getCompetitionListPersistencePort;
    }

    public List<FetchCompetitionDTO> getCompetitions(String userId, boolean organizer) {
        AuthAssertions.assertOrganizer(organizer, userId);

        long now = DateUtils.nowUtcMillis();
        return getCompetitionListPersistencePort.getCompetitions(userId).stream()
                .sorted((a, b) -> compareByNearestStage(a, b, now))
                .map(competition -> toDto(competition, now))
                .toList();
    }

    /**
     * Orders competitions by the "public stages" proximity rule: each competition is represented by
     * its nearest stage (the stage that sorts first under {@link StageProximity}), and competitions are
     * then compared by that representative stage. Competitions with no active stage sort last.
     */
    private int compareByNearestStage(CompetitionSnapshot a, CompetitionSnapshot b, long now) {
        Long aNearest = nearestStageDateFrom(a, now);
        Long bNearest = nearestStageDateFrom(b, now);
        if (aNearest == null || bNearest == null) {
            if (aNearest == null && bNearest == null) {
                return 0;
            }
            return aNearest == null ? 1 : -1;
        }
        return StageProximity.compareByProximity(aNearest, bNearest, now);
    }

    private Long nearestStageDateFrom(CompetitionSnapshot competition, long now) {
        if (competition.stages() == null) {
            return null;
        }
        return competition.stages().stream()
                .filter(stage -> stage.deletedAt() == null)
                .map(StageSnapshot::dateFrom)
                .min((x, y) -> StageProximity.compareByProximity(x, y, now))
                .orElse(null);
    }

    private FetchCompetitionDTO toDto(CompetitionSnapshot competition, long now) {
        return new FetchCompetitionDTO(
                competition.id(),
                competition.name(),
                competition.description(),
                competition.country(),
                competition.address(),
                competition.status(now).name(),
                toStageDtos(competition, now));
    }

    private List<FetchStageDTO> toStageDtos(CompetitionSnapshot competition, long now) {
        if (competition.stages() == null) {
            return List.of();
        }
        return competition.stages().stream()
                .filter(stage -> stage.deletedAt() == null)
                .map(stage -> toStageDto(stage, now))
                .toList();
    }

    private FetchStageDTO toStageDto(StageSnapshot stage, long now) {
        return new FetchStageDTO(
                stage.id(),
                stage.name(),
                stage.dateFrom(),
                stage.dateTo(),
                stage.status(now).name(),
                toEventDtos(stage, now));
    }

    private List<FetchEventDTO> toEventDtos(StageSnapshot stage, long now) {
        if (stage.events() == null) {
            return List.of();
        }
        return stage.events().stream()
                .filter(event -> event.deletedAt() == null)
                .map(event -> new FetchEventDTO(event.id(), event.name(), event.discipline(),
                        event.status(now, stage.dateTo()).name(), event.rank()))
                .toList();
    }
}
