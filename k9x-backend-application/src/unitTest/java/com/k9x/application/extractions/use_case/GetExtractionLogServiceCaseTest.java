package com.k9x.application.extractions.use_case;

import com.k9x.application.extractions.port.GetExtractedCompetitionsPersistencePort;
import com.k9x.application.extractions.use_case.dto.ExtractionLogDayDTO;
import com.k9x.application.extractions.use_case.dto.ExtractionLogEventDTO;
import com.k9x.application.extractions.use_case.dto.ExtractionLogStageDTO;
import com.k9x.domain.competitions.aggregates.CompetitionExtraction;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.competitions.aggregates.CompetitionSource;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetExtractionLogServiceCaseTest {

    private static final long DAY = 86_400_000L;
    private static final long DAY_1 = 1_700_000_000_000L - (1_700_000_000_000L % DAY); // 2023-11-14T00:00Z
    private static final long DAY_2 = DAY_1 + DAY;

    @Mock
    private GetExtractedCompetitionsPersistencePort port;

    private GetExtractionLogServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetExtractionLogServiceCase(port);
    }

    private static CompetitionSnapshot competition(String id, Long loadedAt, Long deletedAt,
                                                   List<StageSnapshot> stages) {
        CompetitionExtraction extraction = loadedAt == null ? null
                : new CompetitionExtraction(id + "-extraction", null, 1L, "FEDERATION_PAGE,cpc", false, loadedAt);
        return new CompetitionSnapshot(id, "Comp " + id, "creator", "Organizer", "ES", "desc", "addr", null, null,
                CompetitionSource.EXTRACTION, extraction, 0L, 0L, deletedAt, stages);
    }

    private static StageSnapshot stage(String id, long from, Long deletedAt, List<EventSnapshot> events) {
        return new StageSnapshot(id, "Stage " + id, "comp", "creator", from, from, 0L, 0L, deletedAt, events);
    }

    private static EventSnapshot event(String id, Long deletedAt, Integer rankScore, List<EventCompetitor> competitors) {
        return new EventSnapshot(id, "obdx-1", "OBDX", "Event " + id, "s-1", "creator",
                null, 0L, 0L, deletedAt, ObdxAvgMethod.AVG, competitors, List.of(), List.of(), List.of(), List.of(),
                rankScore, null, null);
    }

    private static EventCompetitor competitor(String dog) {
        return new EventCompetitor(dog, "Rex", "owner", "Handler", "Team A", "ES", "Border Collie", "ID-001", null,
                null, (short) 1, null, true, false, null, null, null, null, null);
    }

    @Test
    void groups_stages_by_utc_load_day_newest_day_first() {
        when(port.getExtractedCompetitions()).thenReturn(List.of(
                competition("a", DAY_1 + 5_000, null, List.of(stage("a-1", 100L, null, List.of()))),
                competition("b", DAY_2 + DAY - 1, null, List.of(stage("b-1", 200L, null, List.of()))),
                competition("c", DAY_1 + DAY - 1, null, List.of(stage("c-1", 300L, null, List.of())))));

        List<ExtractionLogDayDTO> result = serviceCase.getExtractionLog();

        assertThat(result).extracting(ExtractionLogDayDTO::date).containsExactly(DAY_2, DAY_1);
        assertThat(result.get(0).stages()).extracting(ExtractionLogStageDTO::id).containsExactly("b-1");
        // Within a day the most recent trial comes first.
        assertThat(result.get(1).stages()).extracting(ExtractionLogStageDTO::id).containsExactly("c-1", "a-1");
    }

    @Test
    void maps_events_with_discipline_competitor_count_and_rank_skipping_deleted_ones() {
        EventSnapshot live = event("e-1", null, null, List.of(competitor("dog-1"), competitor("dog-2")));
        EventSnapshot deleted = event("e-2", 999L, null, List.of());
        when(port.getExtractedCompetitions()).thenReturn(List.of(
                competition("a", DAY_1, null, List.of(stage("a-1", 100L, null, List.of(live, deleted))))));

        ExtractionLogStageDTO stage = serviceCase.getExtractionLog().getFirst().stages().getFirst();

        assertThat(stage.competitionName()).isEqualTo("Comp a");
        assertThat(stage.country()).isEqualTo("ES");
        assertThat(stage.events()).hasSize(1);
        ExtractionLogEventDTO event = stage.events().getFirst();
        assertThat(event.id()).isEqualTo("e-1");
        assertThat(event.disciplineId()).isEqualTo("OBDX");
        assertThat(event.competitorCount()).isEqualTo(2);
        assertThat(event.rank()).isNull();
    }

    @Test
    void skips_deleted_competitions_deleted_stages_and_competitions_without_load_date() {
        when(port.getExtractedCompetitions()).thenReturn(List.of(
                competition("deleted", DAY_1, 1L, List.of(stage("d-1", 100L, null, List.of()))),
                competition("unknown", null, null, List.of(stage("u-1", 100L, null, List.of()))),
                competition("live", DAY_1, null, List.of(
                        stage("l-1", 100L, null, List.of()),
                        stage("l-2", 200L, 5L, List.of())))));

        List<ExtractionLogDayDTO> result = serviceCase.getExtractionLog();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().stages()).extracting(ExtractionLogStageDTO::id).containsExactly("l-1");
    }

    @Test
    void returns_empty_when_nothing_was_extracted() {
        when(port.getExtractedCompetitions()).thenReturn(List.of());

        assertThat(serviceCase.getExtractionLog()).isEmpty();
    }
}
