package com.k9x.application.collections.obdx.use_case;

import com.k9x.application.collections.obdx.port.GetObdxCollectionCompetitorsPersistencePort;
import com.k9x.application.collections.obdx.port.GetObdxCollectionExercisesPersistencePort;
import com.k9x.application.collections.obdx.port.GetObdxCollectionScoresPersistencePort;
import com.k9x.application.collections.obdx.use_case.dto.FetchCollectionJudgeScoreDTO;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxCollectionDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionCompetitorDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionExerciseDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionJudgeWithCollectorDTO;
import com.k9x.application.collections.use_case.dto.FetchCollectionScoreDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetObdxCollectionServiceCaseTest {

    @Mock
    private GetObdxCollectionCompetitorsPersistencePort getObdxCollectionCompetitorsPersistencePort;
    @Mock
    private GetObdxCollectionExercisesPersistencePort getObdxCollectionExercisesPersistencePort;
    @Mock
    private GetObdxCollectionScoresPersistencePort getObdxCollectionScoresPersistencePort;
    private GetObdxCollectionServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetObdxCollectionServiceCase(
                getObdxCollectionCompetitorsPersistencePort,
                getObdxCollectionExercisesPersistencePort,
                getObdxCollectionScoresPersistencePort);
    }

    @Test
    void groups_scores_by_competitor_and_exercise() {
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector1@test.com"),
                new FetchCollectionJudgeWithCollectorDTO("judge-2", "Judge Two", "collector2@test.com")
        );
        List<FetchCollectionCompetitorDTO> competitors = List.of(
                new FetchCollectionCompetitorDTO("dog-1", "Rex", "ID-001", "Border Collie", "owner@test.com", "Handler", "Team A", "ES", (short) 1, true, false, null, null, true)
        );
        List<FetchCollectionExerciseDTO> exercises = List.of(new FetchCollectionExerciseDTO("ex-1", (short) 1));
        List<FetchCollectionScoreDTO> scores = List.of(
                new FetchCollectionScoreDTO("dog-1", "ex-1", "judge-1", new BigDecimal("7.5"), null, null),
                new FetchCollectionScoreDTO("dog-1", "ex-1", "judge-2", new BigDecimal("8.0"), null, null)
        );
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(competitors);
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(exercises);
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(scores);

        FetchObdxCollectionDTO result = serviceCase.getCollection("event-1", visibleJudges);

        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().exercises().getFirst().scores())
                .extracting(FetchCollectionJudgeScoreDTO::judgeId)
                .containsExactlyInAnyOrder("judge-1", "judge-2");
    }

    @Test
    void filters_scores_by_visible_judges() {
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com")
        );
        List<FetchCollectionCompetitorDTO> competitors = List.of(
                new FetchCollectionCompetitorDTO("dog-1", "Rex", "ID-001", "Border Collie", "owner@test.com", "Handler", "Team A", "ES", (short) 1, true, false, null, null, true)
        );
        List<FetchCollectionExerciseDTO> exercises = List.of(new FetchCollectionExerciseDTO("ex-1", (short) 1));
        List<FetchCollectionScoreDTO> scores = List.of(
                new FetchCollectionScoreDTO("dog-1", "ex-1", "judge-1", new BigDecimal("7.5"), null, null),
                new FetchCollectionScoreDTO("dog-1", "ex-1", "judge-2", new BigDecimal("8.0"), null, null)
        );
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(competitors);
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(exercises);
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(scores);

        FetchObdxCollectionDTO result = serviceCase.getCollection("event-1", visibleJudges);

        List<FetchCollectionJudgeScoreDTO> exerciseScores =
                result.competitors().getFirst().exercises().getFirst().scores();
        assertThat(exerciseScores).extracting(FetchCollectionJudgeScoreDTO::judgeId).containsExactly("judge-1");
        assertThat(exerciseScores.getFirst().judgeName()).isEqualTo("Judge One");
    }

    @Test
    void returns_visible_judge_with_null_score_when_not_yet_scored() {
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com"),
                new FetchCollectionJudgeWithCollectorDTO("judge-2", "Judge Two", null)
        );
        List<FetchCollectionCompetitorDTO> competitors = List.of(
                new FetchCollectionCompetitorDTO("dog-1", "Rex", "ID-001", "Border Collie", "owner@test.com", "Handler", "Team A", "ES", (short) 1, true, false, null, null, true)
        );
        List<FetchCollectionExerciseDTO> exercises = List.of(new FetchCollectionExerciseDTO("ex-1", (short) 1));
        List<FetchCollectionScoreDTO> scores = List.of(
                new FetchCollectionScoreDTO("dog-1", "ex-1", "judge-1", new BigDecimal("7.5"), null, null)
        );
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(competitors);
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(exercises);
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(scores);

        FetchObdxCollectionDTO result = serviceCase.getCollection("event-1", visibleJudges);

        List<FetchCollectionJudgeScoreDTO> exerciseScores =
                result.competitors().getFirst().exercises().getFirst().scores();
        assertThat(exerciseScores)
                .extracting(FetchCollectionJudgeScoreDTO::judgeId)
                .containsExactly("judge-1", "judge-2");
        assertThat(exerciseScores).filteredOn(s -> s.judgeId().equals("judge-1"))
                .singleElement().extracting(FetchCollectionJudgeScoreDTO::score).isEqualTo(new BigDecimal("7.5"));
        assertThat(exerciseScores).filteredOn(s -> s.judgeId().equals("judge-2"))
                .singleElement().extracting(FetchCollectionJudgeScoreDTO::score).isNull();
    }

    @Test
    void maps_existing_score_row_with_null_value_without_failing() {
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com")
        );
        List<FetchCollectionCompetitorDTO> competitors = List.of(
                new FetchCollectionCompetitorDTO("dog-1", "Rex", "ID-001", "Border Collie", "owner@test.com", "Handler", "Team A", "ES", (short) 1, true, false, null, null, true)
        );
        List<FetchCollectionExerciseDTO> exercises = List.of(new FetchCollectionExerciseDTO("ex-1", (short) 1));
        List<FetchCollectionScoreDTO> scores = List.of(
                new FetchCollectionScoreDTO("dog-1", "ex-1", "judge-1", null, null, null)
        );
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(competitors);
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(exercises);
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(scores);

        FetchObdxCollectionDTO result = serviceCase.getCollection("event-1", visibleJudges);

        List<FetchCollectionJudgeScoreDTO> exerciseScores =
                result.competitors().getFirst().exercises().getFirst().scores();
        assertThat(exerciseScores).extracting(FetchCollectionJudgeScoreDTO::judgeId).containsExactly("judge-1");
        assertThat(exerciseScores.getFirst().score()).isNull();
    }

    @Test
    void sets_enrolled_status_on_competitors() {
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com")
        );
        List<FetchCollectionCompetitorDTO> rawCompetitors = List.of(
                new FetchCollectionCompetitorDTO("dog-1", "Rex", "ID-001", "Border Collie", "owner@test.com", "Handler", "Team A", "ES", (short) 1, true, false, null, null, true)
        );
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(rawCompetitors);
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(List.of());
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(List.of());

        FetchObdxCollectionDTO result = serviceCase.getCollection("event-1", visibleJudges);

        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().competitor().status()).isEqualTo("ENROLLED");
        assertThat(result.competitors().getFirst().competitor().breed()).isEqualTo("Border Collie");
    }

    @Test
    void sets_not_competing_status_on_competitors() {
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com")
        );
        List<FetchCollectionCompetitorDTO> rawCompetitors = List.of(
                new FetchCollectionCompetitorDTO("dog-1", "Rex", "ID-001", "Border Collie", "owner@test.com", "Handler", "Team A", "ES", (short) 1, true, true, null, null, false)
        );
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(rawCompetitors);
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(List.of());
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(List.of());

        FetchObdxCollectionDTO result = serviceCase.getCollection("event-1", visibleJudges);

        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().competitor().status()).isEqualTo("NOT_COMPETING");
    }

    @Test
    void sets_pending_enroll_accept_status_when_not_verified() {
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com")
        );
        List<FetchCollectionCompetitorDTO> rawCompetitors = List.of(
                new FetchCollectionCompetitorDTO("dog-1", "Rex", "ID-001", "Border Collie", "owner@test.com", "Handler", "Team A", "ES", (short) 1, false, false, null, null, true)
        );
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(rawCompetitors);
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(List.of());
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(List.of());

        FetchObdxCollectionDTO result = serviceCase.getCollection("event-1", visibleJudges);

        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().competitor().status()).isEqualTo("PENDING_ENROLL_ACCEPT");
    }

    @Test
    void allows_scores_when_competitor_has_no_cards() {
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com")
        );
        List<FetchCollectionCompetitorDTO> rawCompetitors = List.of(
                new FetchCollectionCompetitorDTO("dog-1", "Rex", "ID-001", "Border Collie", "owner@test.com", "Handler", "Team A", "ES", (short) 1, true, false, null, null, true)
        );
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(rawCompetitors);
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(List.of());
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(List.of());

        FetchObdxCollectionDTO result = serviceCase.getCollection("event-1", visibleJudges);

        assertThat(result.competitors().getFirst().competitor().scoresAllowed()).isTrue();
    }

    @Test
    void disallows_scores_when_competitor_has_two_yellow_cards() {
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com")
        );
        List<FetchCollectionCompetitorDTO> rawCompetitors = List.of(
                new FetchCollectionCompetitorDTO("dog-1", "Rex", "ID-001", "Border Collie", "owner@test.com", "Handler", "Team A", "ES", (short) 1, true, false, null, null, true)
        );
        List<FetchCollectionScoreDTO> scores = List.of(
                new FetchCollectionScoreDTO("dog-1", "ex-1", "judge-1", null, 1000L, null),
                new FetchCollectionScoreDTO("dog-1", "ex-2", "judge-1", null, 2000L, null)
        );
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(rawCompetitors);
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(List.of());
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(scores);

        FetchObdxCollectionDTO result = serviceCase.getCollection("event-1", visibleJudges);

        assertThat(result.competitors().getFirst().competitor().scoresAllowed()).isFalse();
    }

    @Test
    void disallows_scores_when_competitor_has_a_red_card() {
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com")
        );
        List<FetchCollectionCompetitorDTO> rawCompetitors = List.of(
                new FetchCollectionCompetitorDTO("dog-1", "Rex", "ID-001", "Border Collie", "owner@test.com", "Handler", "Team A", "ES", (short) 1, true, false, null, null, true)
        );
        List<FetchCollectionScoreDTO> scores = List.of(
                new FetchCollectionScoreDTO("dog-1", "ex-1", "judge-1", null, null, 1000L)
        );
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(rawCompetitors);
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(List.of());
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(scores);

        FetchObdxCollectionDTO result = serviceCase.getCollection("event-1", visibleJudges);

        assertThat(result.competitors().getFirst().competitor().scoresAllowed()).isFalse();
    }

    @Test
    void disallows_scores_when_competitor_is_not_competing() {
        List<FetchCollectionJudgeWithCollectorDTO> visibleJudges = List.of(
                new FetchCollectionJudgeWithCollectorDTO("judge-1", "Judge One", "collector@test.com")
        );
        List<FetchCollectionCompetitorDTO> rawCompetitors = List.of(
                new FetchCollectionCompetitorDTO("dog-1", "Rex", "ID-001", "Border Collie", "owner@test.com", "Handler", "Team A", "ES", (short) 1, true, true, null, null, true)
        );
        when(getObdxCollectionCompetitorsPersistencePort.getCompetitors("event-1")).thenReturn(rawCompetitors);
        when(getObdxCollectionExercisesPersistencePort.getExercises("event-1")).thenReturn(List.of());
        when(getObdxCollectionScoresPersistencePort.getScores("event-1")).thenReturn(List.of());

        FetchObdxCollectionDTO result = serviceCase.getCollection("event-1", visibleJudges);

        assertThat(result.competitors().getFirst().competitor().scoresAllowed()).isFalse();
    }
}
