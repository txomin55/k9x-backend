package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.obdx.exceptions.ObdxNotEnoughJudgesException;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationExerciseScoreDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationJudgeScoreDTO;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
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
        return event(rows, ObdxAvgMethod.AVG);
    }

    private EventSnapshot event(List<Row> rows, ObdxAvgMethod avgMethod) {
        Set<String> dogIds = new LinkedHashSet<>();
        Set<String> judgeIds = new LinkedHashSet<>();
        List<EventCompetitor> competitors = new ArrayList<>();
        List<EventJudge> judges = new ArrayList<>();
        List<Score> scores = new ArrayList<>();

        for (Row r : rows) {
            if (dogIds.add(r.dogId())) {
                competitors.add(new EventCompetitor(r.dogId(), r.dogName(), "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "identity", (short) 0, false, false, null, null, null));
            }
            if (judgeIds.add(r.judgeId())) {
                judges.add(new EventJudge(r.judgeId(), "Judge " + r.judgeId(), null));
            }
            scores.add(new Score("ex-1", r.judgeId(), r.dogId(), r.score(), 1000L));
        }

        List<EventExercise> exercises = rows.isEmpty()
                ? List.of()
                : List.of(new EventExercise("ex-1", (short) 1, null, List.copyOf(judgeIds)));

        return new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1", "stage-1", "creator@test.com",
                null, 1000L, 1000L, null, avgMethod, competitors, exercises, judges, scores, List.of());
    }

    private record Row(String dogId, String dogName, String judgeId, BigDecimal score) {}

    @Test
    void exposes_obdx_as_its_discipline() {
        assertThat(serviceCase.discipline()).isEqualTo(Discipline.OBDX);
    }

    @Test
    void returns_cached_result_when_ttl_not_expired() {
        FetchObdxClassificationDTO cached = new FetchObdxClassificationDTO(null, List.of(), "AVG", List.of());

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
    void throws_exception_when_mid_avg_and_event_has_fewer_than_4_judges() {
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "identity", (short) 0, false, false, null, null, null));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1", "j-2")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null), new EventJudge("j-2", "Judge j-2", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L),
                new Score("ex-1", "j-2", "dog-1", new BigDecimal("6"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG,
                competitors, exercises, judges, scores, List.of());

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getClassification(event))
                .isInstanceOf(ObdxNotEnoughJudgesException.class);
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
                new Row("dog-1", "Rex", "j-4", new BigDecimal("3"))), ObdxAvgMethod.MID_AVG);

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(midAvgConfig);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        // remove min(3) and max(9), avg(5,7) = 6, * coef(2) = 12
        assertThat(result.competitors().getFirst().totalScore()).isEqualByComparingTo("12.00");
        assertThat(judgeScores(result))
                .extracting(FetchClassificationJudgeScoreDTO::judgeId, FetchClassificationJudgeScoreDTO::applies)
                .containsExactlyInAnyOrder(
                        tuple("j-1", true), tuple("j-2", true), tuple("j-3", false), tuple("j-4", false));
    }

    @Test
    void applies_is_true_for_every_score_under_avg_regardless_of_spread() {
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                List.of(new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "id-1", (short) 1, false, false, null, null, null)),
                List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1", "j-2"))),
                List.of(new EventJudge("j-1", "Judge j-1", null), new EventJudge("j-2", "Judge j-2", null)),
                List.of(new Score("ex-1", "j-1", "dog-1", new BigDecimal("2"), 1000L),
                        new Score("ex-1", "j-2", "dog-1", new BigDecimal("9"), 1000L)),
                List.of());

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(judgeScores(result)).allSatisfy(j -> assertThat(j.applies()).isTrue());
    }

    @Test
    void applies_excludes_the_single_score_under_mid_avg_with_only_one_judge_scored() {
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG,
                List.of(new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "id-1", (short) 1, false, false, null, null, null)),
                List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1", "j-2", "j-3", "j-4"))),
                List.of(new EventJudge("j-1", "Judge j-1", null), new EventJudge("j-2", "Judge j-2", null),
                        new EventJudge("j-3", "Judge j-3", null), new EventJudge("j-4", "Judge j-4", null)),
                List.of(new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L)),
                List.of());

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(judgeScores(result))
                .extracting(FetchClassificationJudgeScoreDTO::judgeId, FetchClassificationJudgeScoreDTO::applies)
                .containsExactly(tuple("j-1", false));
    }

    @Test
    void applies_excludes_both_scores_under_mid_avg_with_only_two_judges_scored() {
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG,
                List.of(new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "id-1", (short) 1, false, false, null, null, null)),
                List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1", "j-2", "j-3", "j-4"))),
                List.of(new EventJudge("j-1", "Judge j-1", null), new EventJudge("j-2", "Judge j-2", null),
                        new EventJudge("j-3", "Judge j-3", null), new EventJudge("j-4", "Judge j-4", null)),
                List.of(new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L),
                        new Score("ex-1", "j-2", "dog-1", new BigDecimal("6"), 1000L)),
                List.of());

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(judgeScores(result))
                .extracting(FetchClassificationJudgeScoreDTO::judgeId, FetchClassificationJudgeScoreDTO::applies)
                .containsExactlyInAnyOrder(tuple("j-1", false), tuple("j-2", false));
    }

    private List<FetchClassificationJudgeScoreDTO> judgeScores(FetchObdxClassificationDTO result) {
        return result.competitors().getFirst().exercises().getFirst().judgeScores();
    }

    @Test
    void averages_only_over_judges_that_score_when_judges_are_split_in_two_rings() {
        // 4 judges in the event roster, but only j-1 and j-2 are assigned to ex-1 (ring 1); j-3
        // and j-4 belong to a different ring and are not assigned to this exercise at all. A ring
        // with fewer than 4 judges can only use AVG (MID_AVG requires >= 4 judges per exercise).
        // The average for ex-1 must be over the assigned judges' scores only (8, 6) -> 7.
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "identity", (short) 0, false, false, null, null, null));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1", "j-2")));
        List<EventJudge> judges = List.of(
                new EventJudge("j-1", "Judge j-1", null),
                new EventJudge("j-2", "Judge j-2", null),
                new EventJudge("j-3", "Judge j-3", null),
                new EventJudge("j-4", "Judge j-4", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L),
                new Score("ex-1", "j-2", "dog-1", new BigDecimal("6"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of());

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
    void exposes_stamped_yellow_cards_per_exercise_with_judge_and_timestamp() {
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "identity", (short) 0, false, false, null, null, null));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1", "j-2")));
        List<EventJudge> judges = List.of(
                new EventJudge("j-1", "Judge j-1", null),
                new EventJudge("j-2", "Judge j-2", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L, 5000L),
                new Score("ex-1", "j-2", "dog-1", new BigDecimal("6"), 1000L, null));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of());

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        // Each stamped row becomes an item, carrying its judge; rows without a card (j-2) are skipped.
        assertThat(result.competitors().getFirst().exercises().getFirst().yellowCards())
                .extracting("judgeId", "timestamp")
                .containsExactlyInAnyOrder(tuple("j-1", 5000L));
        // avg(8, 6) = 7, * coef(3) = 21.00, minus the 10-point yellow card penalty = 11.00
        assertThat(result.competitors().getFirst().exercises().getFirst().totalScore())
                .isEqualByComparingTo("11.00");
    }

    @Test
    void exposes_the_stamped_red_card_of_an_exercise_with_judge_and_timestamp() {
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "identity", (short) 0, false, false, null, null, null));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", null, 1000L, null, 5000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG,
                competitors, exercises, judges, scores, List.of());

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors().getFirst().exercises().getFirst().redCard())
                .extracting("judgeId", "timestamp")
                .containsExactly("j-1", 5000L);
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
    void ranks_red_carded_competitors_after_regular_ones_and_not_competing_last() {
        // dog-1: regular, low score. dog-2: highest score but red-carded, so it must rank after dog-1
        // despite the higher score. dog-3: not competing, ranked last regardless of score.
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "id-1", (short) 1, false, false, null, null, null),
                new EventCompetitor("dog-2", "Max", "owner@test.com", "Handler", "Team B", "ES",
                        "breed", "id-2", (short) 2, false, false, null, null, null),
                new EventCompetitor("dog-3", "Fido", "owner@test.com", "Handler", "Team C", "ES",
                        "breed", "id-3", (short) 3, false, true, null, null, null));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("6"), 1000L),
                new Score("ex-1", "j-1", "dog-2", new BigDecimal("9"), 1000L, null, 5000L),
                new Score("ex-1", "j-1", "dog-3", new BigDecimal("7"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of());

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors()).extracting(FetchClassificationCompetitorDTO::dogId)
                .containsExactly("dog-1", "dog-2", "dog-3");
    }

    @Test
    void exposes_static_start_order_per_competitor_independent_of_ranking() {
        // dog-1 enrolled with start order 5, dog-2 with start order 3. dog-2 scores higher so it
        // ranks 1st, but each competitor must keep its own static start order regardless of the
        // dynamic ranking position.
        List<EventCompetitor> competitors = List.of(
                new EventCompetitor("dog-1", "Rex", "owner@test.com", "Handler", "Team A", "ES",
                        "breed", "id-1", (short) 5, false, false, null, null, null),
                new EventCompetitor("dog-2", "Max", "owner@test.com", "Handler", "Team B", "ES",
                        "breed", "id-2", (short) 3, false, false, null, null, null));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("6"), 1000L),
                new Score("ex-1", "j-1", "dog-2", new BigDecimal("9"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of());

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
                        "breed", "identity", (short) 0, false, false, null, null, null));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1", "j-2")));
        List<EventJudge> judges = List.of(
                new EventJudge("j-1", "Judge j-1", null),
                new EventJudge("j-2", "Judge j-2", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L),
                new Score("ex-1", "j-2", "dog-1", null, 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of());

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
                        "breed", "identity", (short) 0, false, false, null, null, null));
        List<EventExercise> exercises = List.of(
                new EventExercise("ex-1", (short) 1, null, List.of("j-1")),
                new EventExercise("ex-2", (short) 2, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.MID_AVG,
                competitors, exercises, judges, List.of(), List.of());

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
                        "breed", "identity", (short) 0, false, false, null, null, null));
        List<EventExercise> exercises = List.of(
                new EventExercise("ex-1", (short) 1, null, List.of("j-1")),
                new EventExercise("ex-2", (short) 2, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of());

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
                        "breed", "identity", (short) 0, false, false, new BigDecimal("42.00"), null, null));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(new Score("ex-1", "j-1", "dog-1", new BigDecimal("8"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of());

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

    // ---- CACOB / CACIOB awards -------------------------------------------------------------------

    private EventCompetitor competitorWithFciFlag(String dogId, String dogName, short startOrder, Boolean fciConfirmed) {
        return new EventCompetitor(dogId, dogName, "owner@test.com", "Handler", "Team A", "ES",
                "breed", "id-" + dogId, startOrder, false, false, null, null, fciConfirmed);
    }

    @Test
    void awards_cacob_to_first_place_when_fci_confirmed_and_score_rating_above_80() {
        List<EventCompetitor> competitors = List.of(
                competitorWithFciFlag("dog-1", "Rex", (short) 1, true),
                competitorWithFciFlag("dog-2", "Max", (short) 2, false));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("9"), 1000L),
                new Score("ex-1", "j-1", "dog-2", new BigDecimal("6"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of("CACOB"));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(awardsOf(result, "dog-1")).containsExactly("CACOB");
        assertThat(awardsOf(result, "dog-2")).isEmpty();
    }

    @Test
    void awards_rcacob_to_the_best_ranked_fci_confirmed_dog_when_it_is_not_first() {
        List<EventCompetitor> competitors = List.of(
                competitorWithFciFlag("dog-1", "Rex", (short) 1, false),
                competitorWithFciFlag("dog-2", "Max", (short) 2, true));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("9"), 1000L),
                new Score("ex-1", "j-1", "dog-2", new BigDecimal("8.5"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of("CACOB"));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(awardsOf(result, "dog-1")).isEmpty();
        assertThat(awardsOf(result, "dog-2")).containsExactly("RCACOB");
    }

    private List<String> awardsOf(FetchObdxClassificationDTO result, String dogId) {
        return result.competitors().stream()
                .filter(c -> c.dogId().equals(dogId))
                .findFirst().orElseThrow().awards();
    }

    @Test
    void awards_rcacob_to_the_runner_up_when_the_winner_already_took_cacob() {
        // dog-1 wins CACOB outright. dog-2 is the very next qualifying dog (2nd place, confirmed, above 80%)
        // so it takes the reserve award.
        List<EventCompetitor> competitors = List.of(
                competitorWithFciFlag("dog-1", "Rex", (short) 1, true),
                competitorWithFciFlag("dog-2", "Max", (short) 2, true));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("9"), 1000L),
                new Score("ex-1", "j-1", "dog-2", new BigDecimal("8.5"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of("CACOB"));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(awardsOf(result, "dog-1")).containsExactly("CACOB");
        assertThat(awardsOf(result, "dog-2")).containsExactly("RCACOB");
    }

    @Test
    void awards_rcacob_to_the_next_qualifying_dog_skipping_ones_that_do_not_qualify() {
        // dog-1 wins CACOB outright. dog-2 outranks dog-3 but is not FCI-confirmed, so it's skipped;
        // dog-3, further down the ranking, is the next qualifying dog and takes the reserve award.
        List<EventCompetitor> competitors = List.of(
                competitorWithFciFlag("dog-1", "Rex", (short) 1, true),
                competitorWithFciFlag("dog-2", "Max", (short) 2, false),
                competitorWithFciFlag("dog-3", "Fido", (short) 3, true));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("9"), 1000L),
                new Score("ex-1", "j-1", "dog-2", new BigDecimal("8.5"), 1000L),
                new Score("ex-1", "j-1", "dog-3", new BigDecimal("8.1"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of("CACOB"));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(awardsOf(result, "dog-1")).containsExactly("CACOB");
        assertThat(awardsOf(result, "dog-2")).isEmpty();
        assertThat(awardsOf(result, "dog-3")).containsExactly("RCACOB");
    }

    @Test
    void grants_no_award_when_best_ranked_fci_confirmed_dog_is_below_the_score_threshold() {
        // dog-2 is the best-ranked FCI-confirmed dog but only reaches 70%: it blocks the award for
        // everyone behind it (dog-3, also confirmed and above 80%) without qualifying itself.
        List<EventCompetitor> competitors = List.of(
                competitorWithFciFlag("dog-1", "Rex", (short) 1, false),
                competitorWithFciFlag("dog-2", "Max", (short) 2, true),
                competitorWithFciFlag("dog-3", "Fido", (short) 3, true));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(
                new Score("ex-1", "j-1", "dog-1", new BigDecimal("9"), 1000L),
                new Score("ex-1", "j-1", "dog-2", new BigDecimal("7"), 1000L),
                new Score("ex-1", "j-1", "dog-3", new BigDecimal("6"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of("CACOB"));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors()).allSatisfy(c -> assertThat(c.awards()).isEmpty());
    }

    @Test
    void awards_both_cacob_and_caciob_to_the_same_competitor_when_event_enables_both() {
        List<EventCompetitor> competitors = List.of(competitorWithFciFlag("dog-1", "Rex", (short) 1, true));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(new Score("ex-1", "j-1", "dog-1", new BigDecimal("9"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of("CACOB", "CACIOB"));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors().getFirst().awards()).containsExactlyInAnyOrder("CACOB", "CACIOB");
    }

    @Test
    void grants_no_award_when_event_does_not_enable_cacob_or_caciob() {
        List<EventCompetitor> competitors = List.of(competitorWithFciFlag("dog-1", "Rex", (short) 1, true));
        List<EventExercise> exercises = List.of(new EventExercise("ex-1", (short) 1, null, List.of("j-1")));
        List<EventJudge> judges = List.of(new EventJudge("j-1", "Judge j-1", null));
        List<Score> scores = List.of(new Score("ex-1", "j-1", "dog-1", new BigDecimal("9"), 1000L));
        EventSnapshot event = new EventSnapshot("evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1",
                "stage-1", "creator@test.com", null, 1000L, 1000L, null, ObdxAvgMethod.AVG,
                competitors, exercises, judges, scores, List.of());

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);

        FetchObdxClassificationDTO result = serviceCase.getClassification(event);

        assertThat(result.competitors().getFirst().awards()).isEmpty();
    }
}
