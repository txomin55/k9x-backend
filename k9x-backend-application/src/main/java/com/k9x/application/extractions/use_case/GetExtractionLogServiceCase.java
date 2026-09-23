package com.k9x.application.extractions.use_case;

import com.k9x.application.extractions.port.GetExtractedCompetitionsPersistencePort;
import com.k9x.application.extractions.use_case.dto.ExtractionLogDayDTO;
import com.k9x.application.extractions.use_case.dto.ExtractionLogEventDTO;
import com.k9x.application.extractions.use_case.dto.ExtractionLogStageDTO;
import com.k9x.domain.competitions.aggregates.CompetitionExtraction;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.shared.UtcDates;
import com.k9x.domain.stages.aggregates.StageSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Public transparency log of the trials k9x did not collect itself: every active stage of an extracted
 * competition, grouped by the UTC day its extraction was loaded. It asserts nothing about the caller: the
 * endpoint sits outside {@code /secured/}.
 *
 * <p>The day is the load date, not the collection date nor the trial date, because the log answers "what was
 * imported and when". Only the latest extraction of each competition counts — it is the one that describes the
 * data currently loaded — and an EXTRACTION competition with no metadata row has no load date, so it is left
 * out rather than guessed.
 */
public class GetExtractionLogServiceCase {

    private final GetExtractedCompetitionsPersistencePort getExtractedCompetitionsPersistencePort;

    public GetExtractionLogServiceCase(GetExtractedCompetitionsPersistencePort getExtractedCompetitionsPersistencePort) {
        this.getExtractedCompetitionsPersistencePort = getExtractedCompetitionsPersistencePort;
    }

    public List<ExtractionLogDayDTO> getExtractionLog() {
        Map<Long, List<LoadedStage>> stagesByDay = new TreeMap<>(Comparator.reverseOrder());
        getExtractedCompetitionsPersistencePort.getExtractedCompetitions().stream()
                .filter(competition -> competition.deletedAt() == null)
                .filter(competition -> loadedAt(competition) != null)
                .forEach(competition -> competition.stages().stream()
                        .filter(stage -> stage.deletedAt() == null)
                        .forEach(stage -> stagesByDay
                                .computeIfAbsent(UtcDates.startOfUtcDay(loadedAt(competition)),
                                        _ -> new ArrayList<>())
                                .add(new LoadedStage(competition, stage))));

        return stagesByDay.entrySet().stream()
                .map(day -> new ExtractionLogDayDTO(day.getKey(), day.getValue().stream()
                        .sorted(Comparator.comparingLong((LoadedStage ls) -> ls.stage().dateFrom()).reversed())
                        .map(ls -> toStageDto(ls.competition(), ls.stage()))
                        .toList()))
                .toList();
    }

    private static Long loadedAt(CompetitionSnapshot competition) {
        CompetitionExtraction extraction = competition.extraction();
        return extraction == null ? null : extraction.loadedAt();
    }

    private static ExtractionLogStageDTO toStageDto(CompetitionSnapshot competition, StageSnapshot stage) {
        return new ExtractionLogStageDTO(
                stage.id(), stage.name(), competition.name(), competition.country(),
                stage.dateFrom(), stage.dateTo(),
                stage.events().stream()
                        .filter(event -> event.deletedAt() == null)
                        .map(event -> new ExtractionLogEventDTO(
                                event.id(), event.name(), event.discipline(),
                                event.competitors() == null ? 0 : event.competitors().size(),
                                event.rank()))
                        .toList());
    }

    private record LoadedStage(CompetitionSnapshot competition, StageSnapshot stage) {
    }
}
