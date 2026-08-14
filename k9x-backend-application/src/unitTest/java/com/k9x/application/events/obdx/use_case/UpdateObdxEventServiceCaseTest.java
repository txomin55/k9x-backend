package com.k9x.application.events.obdx.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.events.exceptions.EventCategoryNotAllowedException;
import com.k9x.application.events.exceptions.EventCategoryRequiredException;
import com.k9x.application.events.exceptions.EventConfigurationIdRequiredException;
import com.k9x.application.events.obdx.exceptions.BihNotAllowedForSexException;
import com.k9x.application.events.obdx.exceptions.ObdxCollectorNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxDuplicateDogException;
import com.k9x.application.events.obdx.exceptions.ObdxDuplicateExerciseException;
import com.k9x.application.events.obdx.exceptions.ObdxDuplicateJudgeException;
import com.k9x.application.events.obdx.exceptions.ObdxExerciseJudgeNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxExerciseJudgeRequiredException;
import com.k9x.domain.disciplines.obdx.exceptions.ObdxMultipleMainJudgesException;
import com.k9x.domain.disciplines.obdx.exceptions.ObdxNotEnoughJudgesException;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxEventCommand;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.competitions.commands.ObdxEventInfoUpdated;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.disciplines.obdx.ObdxEventCategory;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateObdxEventServiceCaseTest {

    private static final UpdateObdxEventCommand VALID_COMMAND = new UpdateObdxEventCommand(
            "Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), null, ObdxEventCategory.CLUB);

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;
    @Mock
    private SaveCompetitionPersistencePort saveCompetitionPersistencePort;
    @Mock
    private GetUserInfoPersistencePort getUserInfoPersistencePort;
    @Mock
    private GetDogPersistencePort getDogPersistencePort;
    private UpdateObdxEventServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateObdxEventServiceCase(getCompetitionPersistencePort, saveCompetitionPersistencePort,
                getUserInfoPersistencePort, getDogPersistencePort);
    }

    private CompetitionSnapshot competition() {
        EventSnapshot event = new EventSnapshot("event-1", null, null, "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null, null, null);
        // dateFrom after the command's enrollment deadline (2025-01-01) so the deadline-before-start invariant holds.
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", 4102444800000L, Long.MAX_VALUE,
                0L, 0L, null, List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_configuration_id_is_blank() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "  ", 1735689600000L, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), null, ObdxEventCategory.CLUB);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(EventConfigurationIdRequiredException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_collector_email_does_not_exist() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(), List.of(), List.of(new UpdateObdxEventCommand.JudgeCommand("judge-1", "missing@k9x.com", false)), List.of(), null, ObdxEventCategory.CLUB);
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getUserInfoPersistencePort.findById("missing@k9x.com")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxCollectorNotFoundException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void saves_aggregate_when_all_validations_pass() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());

        serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true);

        verify(saveCompetitionPersistencePort).save(any());
    }

    @Test
    void throws_exception_when_judge_is_duplicated() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(), List.of(),
                List.of(new UpdateObdxEventCommand.JudgeCommand("judge-1", null, false),
                        new UpdateObdxEventCommand.JudgeCommand("judge-1", null, false)),
                List.of(), null, ObdxEventCategory.CLUB);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxDuplicateJudgeException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    /**
     * The single-main-judge rule is an aggregate invariant, so the update reaches the domain and is rejected
     * there — nothing is saved.
     */
    @Test
    void throws_exception_when_more_than_one_judge_is_the_main_judge() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(), List.of(),
                List.of(new UpdateObdxEventCommand.JudgeCommand("judge-1", null, true),
                        new UpdateObdxEventCommand.JudgeCommand("judge-2", null, true)),
                List.of(), null, ObdxEventCategory.CLUB);
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxMultipleMainJudgesException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    /** The flag is informative and optional: one main judge saves, and so does none. */
    @Test
    void saves_aggregate_when_a_single_judge_is_the_main_judge() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(), List.of(),
                List.of(new UpdateObdxEventCommand.JudgeCommand("judge-1", null, true),
                        new UpdateObdxEventCommand.JudgeCommand("judge-2", null, false)),
                List.of(), null, ObdxEventCategory.CLUB);
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());

        serviceCase.updateEvent("event-1", command, "user-1", true);

        verify(saveCompetitionPersistencePort).save(any());
    }

    @Test
    void throws_exception_when_exercise_is_duplicated() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(), List.of(new UpdateObdxEventCommand.ExerciseCommand("exercise-1", 1, List.of(), List.of("judge-1")),
                        new UpdateObdxEventCommand.ExerciseCommand("exercise-1", 2, List.of(), List.of("judge-1"))),
                List.of(), List.of(), null, ObdxEventCategory.CLUB);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxDuplicateExerciseException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_exercise_has_no_judges() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(), List.of(new UpdateObdxEventCommand.ExerciseCommand("exercise-1", 1, List.of(), List.of())),
                List.of(), List.of(), null, ObdxEventCategory.CLUB);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxExerciseJudgeRequiredException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_exercise_judge_does_not_exist_in_event() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(),
                List.of(new UpdateObdxEventCommand.ExerciseCommand("exercise-1", 1, List.of(), List.of("judge-1"))),
                List.of(new UpdateObdxEventCommand.JudgeCommand("judge-2", null, false)), List.of(), null, ObdxEventCategory.CLUB);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxExerciseJudgeNotFoundException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_mid_avg_and_an_exercise_has_fewer_than_4_judges() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(),
                List.of(new UpdateObdxEventCommand.ExerciseCommand("exercise-1", 1, List.of(), List.of("judge-1", "judge-2"))),
                List.of(new UpdateObdxEventCommand.JudgeCommand("judge-1", null, false),
                        new UpdateObdxEventCommand.JudgeCommand("judge-2", null, false)), List.of(), null, ObdxEventCategory.CLUB);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxNotEnoughJudgesException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_dog_is_duplicated() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(new UpdateObdxEventCommand.CompetitorCommand("dog-1", 1, null, false, null, false),
                        new UpdateObdxEventCommand.CompetitorCommand("dog-1", 2, null, false, null, false)),
                List.of(), List.of(), List.of(), null, ObdxEventCategory.CLUB);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxDuplicateDogException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_bih_true_for_male_dog() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(new UpdateObdxEventCommand.CompetitorCommand("dog-1", 1, null, true, null, false)), List.of(), List.of(), List.of(), null, ObdxEventCategory.CLUB);
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getDogPersistencePort.getDog("dog-1"))
                .thenReturn(new Dog("dog-1", "id", null, "breed", "Rex", "img", "owner-1", "handler-1", "creator-1", "ES", "team",
                        Sex.MALE, 55, null, 0L, 0L, null));

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(BihNotAllowedForSexException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void computes_rank_score_within_the_category_sub_band() {
        // CPC_COBS band [100, 200]; CLUB takes the lower three quarters [100, 175]; two competitors -> tier 1
        // -> 100 + round(1/3 · 75) = 125.
        UpdateObdxEventCommand command = obdxCommand("OBDX.CPC_COBS.V0", ObdxEventCategory.CLUB);
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competitionInCountry("ES"));
        when(getDogPersistencePort.getDog("dog-es")).thenReturn(dogFrom("dog-es", "ES"));
        when(getDogPersistencePort.getDog("dog-fr")).thenReturn(dogFrom("dog-fr", "FR"));

        serviceCase.updateEvent("event-1", command, "user-1", true);

        assertThat(recordedChange().rankScore()).isEqualTo(125);
    }

    @Test
    void an_open_trial_scores_above_a_club_one_of_the_same_grade() {
        // CPC_COBS OPEN is [176, 200]; two competitors -> tier 1 -> 176 + round(1/3 · 24) = 184.
        UpdateObdxEventCommand command = obdxCommand("OBDX.CPC_COBS.V0", ObdxEventCategory.OPEN);
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competitionInCountry("ES"));
        when(getDogPersistencePort.getDog("dog-es")).thenReturn(dogFrom("dog-es", "ES"));
        when(getDogPersistencePort.getDog("dog-fr")).thenReturn(dogFrom("dog-fr", "FR"));

        serviceCase.updateEvent("event-1", command, "user-1", true);

        assertThat(recordedChange().rankScore()).isEqualTo(184);
    }

    @Test
    void a_world_championship_final_scores_1000_whatever_its_size() {
        UpdateObdxEventCommand command = obdxCommand("OBDX.FCI_GRADE_3.V0", ObdxEventCategory.WC_FINAL);
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competitionInCountry("ES"));
        when(getDogPersistencePort.getDog("dog-es")).thenReturn(dogFrom("dog-es", "ES"));
        when(getDogPersistencePort.getDog("dog-fr")).thenReturn(dogFrom("dog-fr", "FR"));

        serviceCase.updateEvent("event-1", command, "user-1", true);

        assertThat(recordedChange().rankScore()).isEqualTo(1000);
    }

    @Test
    void rejects_a_championship_category_on_a_grade_that_does_not_host_one() {
        UpdateObdxEventCommand command = obdxCommand("OBDX.FCI_GRADE_2.V0", ObdxEventCategory.WC_FINAL);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(EventCategoryNotAllowedException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void rejects_an_event_with_no_category() {
        UpdateObdxEventCommand command = obdxCommand("OBDX.CPC_COBS.V0", null);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(EventCategoryRequiredException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void leaves_rank_score_null_when_the_configuration_has_no_band() {
        // "config-1" is not a known configuration, so no band -> null score, and any category is accepted.
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());

        serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true);

        assertThat(recordedChange().rankScore()).isNull();
    }

    private UpdateObdxEventCommand obdxCommand(String configurationId, ObdxEventCategory category) {
        return new UpdateObdxEventCommand("Event 1", configurationId, 1735689600000L, ObdxAvgMethod.AVG,
                List.of(new UpdateObdxEventCommand.CompetitorCommand("dog-es", 1, null, false, null, false),
                        new UpdateObdxEventCommand.CompetitorCommand("dog-fr", 2, null, false, null, false)),
                List.of(), List.of(), List.of(), null, category);
    }

    private ObdxEventInfoUpdated recordedChange() {
        ArgumentCaptor<CompetitionAggregate> captor = ArgumentCaptor.forClass(CompetitionAggregate.class);
        verify(saveCompetitionPersistencePort).save(captor.capture());
        return captor.getValue().pendingChanges().stream()
                .filter(ObdxEventInfoUpdated.class::isInstance)
                .map(ObdxEventInfoUpdated.class::cast)
                .findFirst()
                .orElseThrow();
    }

    private CompetitionSnapshot competitionInCountry(String country) {
        EventSnapshot event = new EventSnapshot("event-1", null, null, "Event 1", "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null, null, null);
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", 4102444800000L, Long.MAX_VALUE,
                0L, 0L, null, List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", country, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    private Dog dogFrom(String id, String country) {
        return new Dog(id, "id-" + id, null, "breed", "Rex", "img", "owner-1", "handler-1", "creator-1", country, "team",
                Sex.FEMALE, 55, null, 0L, 0L, null);
    }
}
