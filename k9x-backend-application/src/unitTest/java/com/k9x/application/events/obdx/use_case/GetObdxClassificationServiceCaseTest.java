package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.ObdxClassificationConfigDTO;
import com.k9x.application.events.obdx.use_case.port.ClassificationCacheManagerPort;
import com.k9x.domain.aggregates.disciplines.ClassificationCacheEvictStrategy;
import com.k9x.domain.aggregates.disciplines.Discipline;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.events.EventCompetitor;
import com.k9x.domain.aggregates.events.EventExercise;
import com.k9x.domain.aggregates.events.EventJudge;
import com.k9x.domain.aggregates.events.Score;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetObdxClassificationServiceCaseTest {

    private static final ObdxClassificationConfigDTO CONFIG = new ObdxClassificationConfigDTO(
            ClassificationCacheEvictStrategy.OBDX,
            new BigDecimal("10"),
            Map.of("ex-1", new BigDecimal("3"), "ex-2", new BigDecimal("4")),
            List.of("ex-1"),
            List.of("ex-2"));

    @Mock
    private GetObdxClassificationConfigPort getObdxClassificationConfigPort;
    @Mock
    private ClassificationCacheManagerPort classificationCacheManagerPort;

    private GetObdxClassificationServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetObdxClassificationServiceCase(
                getObdxClassificationConfigPort, classificationCacheManagerPort);
    }

    private Event emptyEvent() {
        return event(List.of());
    }

    /**
     * Builds an Event whose competitors/exercises/judges/scores reproduce the same scenario the old test
     * fed through FetchClassificationRawRowDTO rows. Each row is (dogId, dogName, judgeId, score) on the
     * single exercise "ex-1" (position 1, no tags), score lastUpdate 1000L.
     */
    private Event event(List<Row> rows) {
        Set<String> dogIds = new LinkedHashSet<>();
        Set<String> judgeIds = new LinkedHashSet<>();
        List<EventCompetitor> competitors = new ArrayList<>();
        List<EventJudge> judges = new ArrayList<>();
        List<Score> scores = new ArrayList<>();

        for (Row r : rows) {
            if (dogIds.add(r.dogId())) {
                competitors.add(new EventCompetitor(r.dogId(), r.dogName(), "owner@test.com", "Team A", "ES",
                        "breed", "identity", (short) 0, false, false));
            }
            if (judgeIds.add(r.judgeId())) {
                judges.add(new EventJudge(r.judgeId(), "Judge " + r.judgeId(), null));
            }
            scores.add(new Score("ex-1", r.judgeId(), r.dogId(), r.score(), 1000L));
        }

        List<EventExercise> exercises = rows.isEmpty()
                ? List.of()
                : List.of(new EventExercise("ex-1", (short) 1, null));

        return new Event("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1", "stage-1", "creator@test.com",
                1000L, 1000L, null, ObdxAvgMethod.MID_AVG, competitors, exercises, judges, scores);
    }

    private record Row(String dogId, String dogName, String judgeId, BigDecimal score) {}

    @Test
    void exposes_obdx_as_its_discipline() {
        assertThat(serviceCase.discipline()).isEqualTo(Discipline.OBDX);
    }

    @Test
    void returns_cached_result_when_ttl_not_expired() {
        FetchObdxClassificationDTO cached = new FetchObdxClassificationDTO(null, List.of());

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(cached);

        FetchObdxClassificationDTO result = serviceCase.getClassification(emptyEvent());

        assertThat(result).isSameAs(cached);
        verify(classificationCacheManagerPort, never()).put(any(), any());
    }

    @Test
    void recomputes_and_caches_when_cache_miss() {
        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        serviceCase.getClassification(emptyEvent());

        verify(classificationCacheManagerPort).put(eq("evt-1"), any());
    }

    @Test
    void applies_avg_multiplied_by_coef_when_fewer_than_4_judges() {
        Event event = event(List.of(
                new Row("dog-1", "Rex", "j-1", new BigDecimal("8")),
                new Row("dog-1", "Rex", "j-2", new BigDecimal("6"))));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        // avg(8,6) = 7, * coef(3) = 21
        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().totalScore()).isEqualByComparingTo("21.00");
    }

    @Test
    void applies_mid_avg_removing_min_and_max_when_4_or_more_judges() {
        ObdxClassificationConfigDTO midAvgConfig = new ObdxClassificationConfigDTO(
                ClassificationCacheEvictStrategy.OBDX,
                new BigDecimal("10"),
                Map.of("ex-1", new BigDecimal("2")), List.of(), List.of());

        Event event = event(List.of(
                new Row("dog-1", "Rex", "j-1", new BigDecimal("5")),
                new Row("dog-1", "Rex", "j-2", new BigDecimal("7")),
                new Row("dog-1", "Rex", "j-3", new BigDecimal("9")),
                new Row("dog-1", "Rex", "j-4", new BigDecimal("3"))));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(midAvgConfig);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        // remove min(3) and max(9), avg(5,7) = 6, * coef(2) = 12
        assertThat(result.competitors().getFirst().totalScore()).isEqualByComparingTo("12.00");
    }

    @Test
    void assigns_positions_sorted_by_total_score_descending() {
        Event event = event(List.of(
                new Row("dog-1", "Rex", "j-1", new BigDecimal("6")),
                new Row("dog-2", "Max", "j-1", new BigDecimal("9"))));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors()).hasSize(2);
        assertThat(result.competitors().get(0).dogId()).isEqualTo("dog-2");
        assertThat(result.competitors().get(0).position()).isEqualTo(1);
        assertThat(result.competitors().get(1).dogId()).isEqualTo("dog-1");
        assertThat(result.competitors().get(1).position()).isEqualTo(2);
    }

    @Test
    void tied_dogs_get_same_position() {
        ObdxClassificationConfigDTO tieConfig = new ObdxClassificationConfigDTO(
                ClassificationCacheEvictStrategy.OBDX,
                new BigDecimal("10"),
                Map.of("ex-1", new BigDecimal("1")), List.of(), List.of());

        Event event = event(List.of(
                new Row("dog-1", "Rex", "j-1", new BigDecimal("7")),
                new Row("dog-2", "Max", "j-1", new BigDecimal("7"))));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(tieConfig);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors()).hasSize(2);
        assertThat(result.competitors().get(0).position()).isEqualTo(1);
        assertThat(result.competitors().get(1).position()).isEqualTo(1);
    }
}
