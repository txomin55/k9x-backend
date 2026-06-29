package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.GetCompetitionListPersistencePort;
import com.k9x.application.competitions.use_case.dto.FetchCompetitionDTO;
import com.k9x.application.competitions.use_case.dto.FetchEventDTO;
import com.k9x.application.competitions.use_case.dto.FetchStageDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.exceptions.UnauthorizedResourceException;

import java.util.List;

public class GetCompetitionListServiceCase {

    private final GetCompetitionListPersistencePort getCompetitionListPersistencePort;

    public GetCompetitionListServiceCase(GetCompetitionListPersistencePort getCompetitionListPersistencePort) {
        this.getCompetitionListPersistencePort = getCompetitionListPersistencePort;
    }

    public List<FetchCompetitionDTO> getCompetitions(String userId, boolean organizer) {
        if (!organizer) {
            throw new UnauthorizedResourceException();
        }

        long now = DateUtils.nowUtcMillis();
        return getCompetitionListPersistencePort.getCompetitions(userId).stream()
                .map(competition -> toDto(competition, now))
                .toList();
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
                toEventDtos(stage));
    }

    private List<FetchEventDTO> toEventDtos(StageSnapshot stage) {
        if (stage.events() == null) {
            return List.of();
        }
        return stage.events().stream()
                .filter(event -> event.deletedAt() == null)
                .map(event -> new FetchEventDTO(event.id(), event.name(), event.discipline(), event.status().name()))
                .toList();
    }
}
