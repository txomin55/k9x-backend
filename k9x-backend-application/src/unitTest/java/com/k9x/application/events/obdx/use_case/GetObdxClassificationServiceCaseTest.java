package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationExerciseScoreDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.ObdxClassificationConfigDTO;
import com.k9x.application.events.obdx.use_case.port.ClassificationCacheManagerPort;
import com.k9x.domain.disciplines.valueobjects.ClassificationCacheEvictStrategy;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.EventJudge;
import com.k9x.domain.events.valueobjects.Score;
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

    private EventSnapshot emptyEvent() {
        return event(List.of());
    }

    /**
     * Builds an EventSnapshot whose competitors/exercises/judges/scores reproduce the same scenario the old test
     * fed through FetchClassificationRawRowDTO rows. Each row is (dogId, dogName, judgeId, score) on the
     * single exercise "ex-1" (position 1, no tags), score lastUpdate 1000L.
     */
    private EventSnapshot event(List<Row> rows) {
        Set<String> dogIds = new LinkedHashSet<>();
        Set<String> judgeIds = new LinkedHashSet<>();
        List<EventCompetitor> competitors = new ArrayList<>();
        List<EventJudge> judges = new ArrayList<>();
        List<Score> scores = new ArrayList<>();

        for (Row r : rows) {
            if (dogIds.add(r.dogId())) {
                competitors.add(new EventCompetitor(r.dogId(), r.dogName(), "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "identity", (short) 0, false, false, null));
            }
            if (judgeIds.add(r.judgeId())) {
                judges.add(new EventJudge(r.judgeId(), "Judge " + r.judgeId(), null));
            }
            scores.add(new Score("ex-1", r.judgeId(), r.dogId(), r.score(), 1000L));
        }

        List<EventExercise> exercises = rows.isEmpty()
                ? List.of()
                : List.of(new EventExercise("ex-1", (short) 1, null));

        return new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1", "stage-1", "creator@test.com",
                null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG, competitors, exercises, judges, scores);
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
        EventSnapshot event = event(List.of(
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

        EventSnapshot event = event(List.of(
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
    void averages_only_over_judges_that_score_when_judges_are_split_in_two_rings() {
        // 4 judges, 2 per ring. dog-1 competes in ring 1 (j-1, j-2); the ring-2 judges
        // (j-3, j-4) are part of the event but DO NOT score this dog's exercise, so their
        // score rows are null (as produced by the cartesian product on the persistence side).
        // The average for ex-1 must be over the present scores only (8, 6) -> 7, NOT diluted
        // by the absent judges (would be (8+6+0+0)/4 = 3.5 if counted as 0) and NOT trimmed by
        // MID_AVG (which only triggers with 4+ PRESENT scores).
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "identity", (short) 0, false, false, null));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null));
        List<EventJudge> judges = List.of(
                new EventJudge("j-1", "Judge j-1", null),
                new EventJudge("j-2", "Judge j-2", null),
                new EventJudge("j-3", "Judge j-3", null),
                new EventJudge("j-4", "Judge j-4", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L),
                new Score("ex-1", "j-2", "dog-1", new BigDecimal("6"), 1000L),
                new Score("ex-1", "j-3", "dog-1", null, 1000L),
                new Score("ex-1", "j-4", "dog-1", null, 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG,
                competitors, exercises, judges, scores);

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        // avg(8, 6) = 7, * coef(3) = 21.00
        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().totalScore()).isEqualByComparingTo("21.00");
        // only the two present judges appear in the exercise breakdown
        assertThat(result.competitors().getFirst().exercises().getFirst().judgeScores())
                .extracting("judgeId").containsExactlyInAnyOrder("j-1", "j-2");
    }

    @Test
    void each_dog_is_averaged_over_its_own_ring_judges() {
        // dog-1 judged by ring 1 (j-1, j-2); dog-2 judged by ring 2 (j-3, j-4).
        // Each dog's average must use only its ring's scores, never the other ring's absent judges.
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "id-1", (short) 0, false, false, null),
                new EventCompetitor("dog-2", "Max", "owner@test.com", "Handler", "Team B", "ES",
                        "breed", "id-2", (short) 0, false, false, null));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null));
        List<EventJudge> judges = List.of(
                new EventJudge("j-1", "Judge j-1", null),
                new EventJudge("j-2", "Judge j-2", null),
                new EventJudge("j-3", "Judge j-3", null),
                new EventJudge("j-4", "Judge j-4", null));
        List<Score> scores = List.of(
                // dog-1 → ring 1 present, ring 2 absent
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L),
                new Score("ex-1", "j-2", "dog-1", new BigDecimal("6"), 1000L),
                new Score("ex-1", "j-3", "dog-1", null, 1000L),
                new Score("ex-1", "j-4", "dog-1", null, 1000L),
                // dog-2 → ring 2 present, ring 1 absent
                new Score("ex-1", "j-1", "dog-2", null, 1000L),
                new Score("ex-1", "j-2", "dog-2", null, 1000L),
                new Score("ex-1", "j-3", "dog-2", new BigDecimal("9"), 1000L),
                new Score("ex-1", "j-4", "dog-2", new BigDecimal("7"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG,
                competitors, exercises, judges, scores);

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        // dog-2: avg(9,7)=8 *3 = 24.00 (1st); dog-1: avg(8,6)=7 *3 = 21.00 (2nd)
        assertThat(result.competitors()).hasSize(2);
        assertThat(result.competitors().get(0).dogId()).isEqualTo("dog-2");
        assertThat(result.competitors().get(0).totalScore()).isEqualByComparingTo("24.00");
        assertThat(result.competitors().get(1).dogId()).isEqualTo("dog-1");
        assertThat(result.competitors().get(1).totalScore()).isEqualByComparingTo("21.00");
    }

    @Test
    void assigns_positions_sorted_by_total_score_descending() {
        EventSnapshot event = event(List.of(
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
    void exposes_static_start_order_per_competitor_independent_of_ranking() {
        // dog-1 enrolled with start order 5, dog-2 with start order 3. dog-2 scores higher so it
        // ranks 1st, but each competitor must keep its own static start order regardless of the
        // dynamic ranking position.
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "id-1", (short) 5, false, false, null),
                new EventCompetitor("dog-2", "Max", "owner@test.com", "Handler", "Team B", "ES",
                        "breed", "id-2", (short) 3, false, false, null));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("6"), 1000L),
                new Score("ex-1", "j-1", "dog-2", new BigDecimal("9"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG,
                competitors, exercises, judges, scores);

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors()).hasSize(2);
        // dog-2 ranks 1st but keeps its static start order 3
        assertThat(result.competitors().get(0).dogId()).isEqualTo("dog-2");
        assertThat(result.competitors().get(0).position()).isEqualTo(1);
        assertThat(result.competitors().get(0).startOrder()).isEqualTo((short) 3);
        // dog-1 ranks 2nd but keeps its static start order 5
        assertThat(result.competitors().get(1).dogId()).isEqualTo("dog-1");
        assertThat(result.competitors().get(1).position()).isEqualTo(2);
        assertThat(result.competitors().get(1).startOrder()).isEqualTo((short) 5);
    }

    @Test
    void marks_competitor_as_settled_when_all_exercise_judge_scores_present() {
        // single exercise, single judge, dog scored -> required (1*1) met -> SETTLED.
        EventSnapshot event = event(List.of(new Row("dog-1", "Rex", "j-1", new BigDecimal("8"))));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().status()).isEqualTo("SETTLED");
    }

    @Test
    void marks_competitor_as_live_when_scores_still_missing() {
        // one exercise, two judges, but only j-1 has scored -> required (1*2) not met -> LIVE.
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "identity", (short) 0, false, false, null));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null));
        List<EventJudge> judges = List.of(
                new EventJudge("j-1", "Judge j-1", null),
                new EventJudge("j-2", "Judge j-2", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L),
                new Score("ex-1", "j-2", "dog-1", null, 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG,
                competitors, exercises, judges, scores);

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().status()).isEqualTo("LIVE");
    }

    @Test
    void marks_competitor_as_pending_with_zero_points_but_exposes_max_per_exercise_when_unscored() {
        // dog-1 is enrolled but no score has been recorded for it. It must be PENDING (not LIVE) with 0 points,
        // while each exercise still exposes its maximum: exerciseScore ex-1 -> 10*coef(3)=30, ex-2 -> 10*coef(4)=40.
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Luna", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "identity", (short) 0, false, false, null));
        List<EventExercise> exercises = List.of(
                new EventExercise("ex-1", (short) 1, null),
                new EventExercise("ex-2", (short) 2, null));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG,
                competitors, exercises, judges, List.of());

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors()).hasSize(1);
        FetchClassificationCompetitorDTO competitor = result.competitors().getFirst();
        assertThat(competitor.status()).isEqualTo("PENDING");
        assertThat(competitor.totalScore()).isEqualByComparingTo("0.00");
        assertThat(competitor.scoreRating()).isEqualByComparingTo("0.00");
        // each exercise exposes its max (exerciseScore) but a null achieved value (totalScore) while unscored
        assertThat(competitor.exercises())
                .extracting(FetchClassificationExerciseScoreDTO::exerciseScore)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactlyInAnyOrder(new BigDecimal("30"), new BigDecimal("40"));
        assertThat(competitor.exercises())
                .allSatisfy(e -> {
                    assertThat(e.totalScore()).isNull();
                    assertThat(e.scoreRating()).isNull();
                });
    }

    @Test
    void scored_exercise_uses_average_times_coef_while_unscored_one_stays_at_zero() {
        // dog-1 has been scored on ex-1 (8) but not on ex-2. ex-1 -> avg(8)*coef(3)=24, ex-2 -> 0, total 24.00.
        // The competitor has started, so it is LIVE (not settled: ex-2 still missing).
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "identity", (short) 0, false, false, null));
        List<EventExercise> exercises = List.of(
                new EventExercise("ex-1", (short) 1, null),
                new EventExercise("ex-2", (short) 2, null));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG,
                competitors, exercises, judges, scores);

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().status()).isEqualTo("LIVE");
        assertThat(result.competitors().getFirst().totalScore()).isEqualByComparingTo("24.00");
    }

    @Test
    void uses_manual_final_score_as_total_score_when_present_ignoring_computed_scores() {
        // dog-1 has a manually set final score of 42.00. Even though its judge scores would compute to
        // avg(8)*coef(3)=24.00, the returned totalScore must be the manual final score.
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "identity", (short) 0, false, false, new BigDecimal("42.00")));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG,
                competitors, exercises, judges, scores);

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().totalScore()).isEqualByComparingTo("42.00");
    }

    @Test
    void tied_dogs_get_same_position() {
        ObdxClassificationConfigDTO tieConfig = new ObdxClassificationConfigDTO(
                ClassificationCacheEvictStrategy.OBDX,
                new BigDecimal("10"),
                Map.of("ex-1", new BigDecimal("1")), List.of(), List.of());

        EventSnapshot event = event(List.of(
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
