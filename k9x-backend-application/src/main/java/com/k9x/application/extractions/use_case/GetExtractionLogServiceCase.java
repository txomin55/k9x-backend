package com.k9x.application.extractions.use_case;

import com.k9x.application.extractions.port.GetExtractedCompetitionsPersistencePort;
import com.k9x.application.extractions.use_case.dto.ExtractionLogDayDTO;
import com.k9x.application.extractions.use_case.dto.ExtractionLogEventDTO;
import com.k9x.application.extractions.use_case.dto.ExtractionLogStageDTO;
import com.k9x.application.extractions.use_case.dto.FetchExtractionLogStageDTO;
import com.k9x.domain.disciplines.obdx.ObdxRank;
import com.k9x.domain.shared.UtcDates;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The public log of imported trials, grouped by the UTC day they were loaded, newest day first. It reads its own
 * query projection ({@link FetchExtractionLogStageDTO}), not the competition aggregate.
 */
public class GetExtractionLogServiceCase {

    private final GetExtractedCompetitionsPersistencePort getExtractedCompetitionsPersistencePort;

    public GetExtractionLogServiceCase(GetExtractedCompetitionsPersistencePort getExtractedCompetitionsPersistencePort) {
        this.getExtractedCompetitionsPersistencePort = getExtractedCompetitionsPersistencePort;
    }

    public List<ExtractionLogDayDTO> getExtractionLog() {
        Map<Long, List<FetchExtractionLogStageDTO>> stagesByDay = new TreeMap<>(Comparator.reverseOrder());
        getExtractedCompetitionsPersistencePort.getExtractedStages().forEach(stage -> stagesByDay
                .computeIfAbsent(UtcDates.startOfUtcDay(stage.loadedAt()), _ -> new ArrayList<>())
                .add(stage));

        return stagesByDay.entrySet().stream()
                .map(day -> new ExtractionLogDayDTO(day.getKey(), day.getValue().stream()
                        .sorted(Comparator.comparingLong(FetchExtractionLogStageDTO::dateFrom).reversed())
                        .map(GetExtractionLogServiceCase::toStageDto)
                        .toList()))
                .toList();
    }

    private static ExtractionLogStageDTO toStageDto(FetchExtractionLogStageDTO stage) {
        return new ExtractionLogStageDTO(
                stage.id(), stage.name(), stage.competitionName(), stage.country(),
                stage.dateFrom(), stage.dateTo(),
                stage.events().stream()
                        .map(event -> new ExtractionLogEventDTO(
                                event.id(), event.name(), event.disciplineId(), event.competitorCount(),
                                event.rankScore() == null ? null : ObdxRank.labelFromScore(event.rankScore())))
                        .toList());
    }
}
