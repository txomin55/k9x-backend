package com.k9x.application.extractions.use_case;

import com.k9x.application.extractions.port.GetExtractedCompetitionsPersistencePort;
import com.k9x.application.extractions.use_case.dto.ExtractionLogDayDTO;
import com.k9x.application.extractions.use_case.dto.ExtractionLogEventDTO;
import com.k9x.application.extractions.use_case.dto.ExtractionLogStageDTO;
import com.k9x.application.extractions.use_case.dto.FetchExtractionLogEventDTO;
import com.k9x.application.extractions.use_case.dto.FetchExtractionLogStageDTO;
import com.k9x.domain.disciplines.obdx.ObdxRank;
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

    private static FetchExtractionLogStageDTO stage(String id, long from, long loadedAt,
                                                    List<FetchExtractionLogEventDTO> events) {
        return new FetchExtractionLogStageDTO(id, "Stage " + id, "Comp " + id, "ES", from, from, loadedAt, events);
    }

    @Test
    void groups_stages_by_utc_load_day_newest_day_first() {
        when(port.getExtractedStages()).thenReturn(List.of(
                stage("a-1", 100L, DAY_1 + 5_000, List.of()),
                stage("b-1", 200L, DAY_2 + DAY - 1, List.of()),
                stage("c-1", 300L, DAY_1 + DAY - 1, List.of())));

        List<ExtractionLogDayDTO> result = serviceCase.getExtractionLog();

        assertThat(result).extracting(ExtractionLogDayDTO::date).containsExactly(DAY_2, DAY_1);
        assertThat(result.get(0).stages()).extracting(ExtractionLogStageDTO::id).containsExactly("b-1");
        // Within a day the most recent trial comes first.
        assertThat(result.get(1).stages()).extracting(ExtractionLogStageDTO::id).containsExactly("c-1", "a-1");
    }

    @Test
    void maps_events_with_discipline_competitor_count_and_rank() {
        when(port.getExtractedStages()).thenReturn(List.of(stage("a-1", 100L, DAY_1, List.of(
                new FetchExtractionLogEventDTO("e-1", "Event e-1", "OBDX", 2, null),
                new FetchExtractionLogEventDTO("e-2", "Event e-2", "OBDX", 7, 900)))));

        ExtractionLogStageDTO stage = serviceCase.getExtractionLog().getFirst().stages().getFirst();

        assertThat(stage.competitionName()).isEqualTo("Comp a-1");
        assertThat(stage.country()).isEqualTo("ES");
        assertThat(stage.events()).extracting(ExtractionLogEventDTO::id).containsExactly("e-1", "e-2");
        ExtractionLogEventDTO unranked = stage.events().getFirst();
        assertThat(unranked.disciplineId()).isEqualTo("OBDX");
        assertThat(unranked.competitorCount()).isEqualTo(2);
        assertThat(unranked.rank()).isNull();
        assertThat(stage.events().get(1).rank()).isEqualTo(ObdxRank.labelFromScore(900));
    }

    @Test
    void returns_empty_when_nothing_was_extracted() {
        when(port.getExtractedStages()).thenReturn(List.of());

        assertThat(serviceCase.getExtractionLog()).isEmpty();
    }
}
