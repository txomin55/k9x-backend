package com.k9x.application.events.obdx.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.events.exceptions.EventConfigurationIdRequiredException;
import com.k9x.application.events.obdx.exceptions.BihNotAllowedForSexException;
import com.k9x.application.events.obdx.exceptions.ObdxCollectorNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxDuplicateDogException;
import com.k9x.application.events.obdx.exceptions.ObdxDuplicateExerciseException;
import com.k9x.application.events.obdx.exceptions.ObdxDuplicateJudgeException;
import com.k9x.application.events.obdx.exceptions.ObdxExerciseJudgeNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxExerciseJudgeRequiredException;
import com.k9x.application.events.obdx.exceptions.ObdxNotEnoughJudgesException;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxEventCommand;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.competitions.commands.ObdxEventInfoUpdated;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
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
            "Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());

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
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null, null);
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
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "  ", 1735689600000L, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());

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
                List.of(), List.of(), List.of(new UpdateObdxEventCommand.JudgeCommand("judge-1", "missing@k9x.com")), List.of());
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
                List.of(new UpdateObdxEventCommand.JudgeCommand("judge-1", null),
                        new UpdateObdxEventCommand.JudgeCommand("judge-1", null)),
                List.of());

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxDuplicateJudgeException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_exercise_is_duplicated() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(), List.of(new UpdateObdxEventCommand.ExerciseCommand("exercise-1", 1, List.of(), List.of("judge-1")),
                        new UpdateObdxEventCommand.ExerciseCommand("exercise-1", 2, List.of(), List.of("judge-1"))),
                List.of(), List.of());

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxDuplicateExerciseException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_exercise_has_no_judges() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(), List.of(new UpdateObdxEventCommand.ExerciseCommand("exercise-1", 1, List.of(), List.of())),
                List.of(), List.of());

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxExerciseJudgeRequiredException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_exercise_judge_does_not_exist_in_event() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(),
                List.of(new UpdateObdxEventCommand.ExerciseCommand("exercise-1", 1, List.of(), List.of("judge-1"))),
                List.of(new UpdateObdxEventCommand.JudgeCommand("judge-2", null)), List.of());

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxExerciseJudgeNotFoundException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_mid_avg_and_an_exercise_has_fewer_than_4_judges() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(),
                List.of(new UpdateObdxEventCommand.ExerciseCommand("exercise-1", 1, List.of(), List.of("judge-1", "judge-2"))),
                List.of(new UpdateObdxEventCommand.JudgeCommand("judge-1", null),
                        new UpdateObdxEventCommand.JudgeCommand("judge-2", null)), List.of());

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxNotEnoughJudgesException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_dog_is_duplicated() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(new UpdateObdxEventCommand.CompetitorCommand("dog-1", 1, null, false, false),
                        new UpdateObdxEventCommand.CompetitorCommand("dog-1", 2, null, false, false)),
                List.of(), List.of(), List.of());

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxDuplicateDogException.class);

        verifyNoInteractions(getCompetitionPersistencePort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_bih_true_for_male_dog() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, ObdxAvgMethod.MID_AVG,
                List.of(new UpdateObdxEventCommand.CompetitorCommand("dog-1", 1, null, true, false)), List.of(), List.of(), List.of());
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getDogPersistencePort.getDog("dog-1"))
                .thenReturn(new Dog("dog-1", "id", "breed", "Rex", "img", "owner-1", "handler-1", "creator-1", "ES", "team",
                        Sex.MALE, 55, null, 0L, 0L, null));

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(BihNotAllowedForSexException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void computes_international_plus_rank_when_a_competitor_is_from_another_country() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L,
                ObdxAvgMethod.AVG,
                List.of(new UpdateObdxEventCommand.CompetitorCommand("dog-es", 1, null, false, false),
                        new UpdateObdxEventCommand.CompetitorCommand("dog-fr", 2, null, false, false)),
                List.of(), List.of(), List.of());
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competitionInCountry("ES"));
        when(getDogPersistencePort.getDog("dog-es")).thenReturn(dogFrom("dog-es", "ES"));
        when(getDogPersistencePort.getDog("dog-fr")).thenReturn(dogFrom("dog-fr", "FR"));

        serviceCase.updateEvent("event-1", command, "user-1", true);

        // 2 competitors -> E; a competitor from FR while the event is in ES -> international -> "E+".
        assertThat(recordedRank()).isEqualTo("E+");
    }

    @Test
    void computes_national_rank_when_every_competitor_shares_the_event_country() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L,
                ObdxAvgMethod.AVG,
                List.of(new UpdateObdxEventCommand.CompetitorCommand("dog-es", 1, null, false, false),
                        new UpdateObdxEventCommand.CompetitorCommand("dog-es-2", 2, null, false, false)),
                List.of(), List.of(), List.of());
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competitionInCountry("ES"));
        when(getDogPersistencePort.getDog("dog-es")).thenReturn(dogFrom("dog-es", "ES"));
        when(getDogPersistencePort.getDog("dog-es-2")).thenReturn(dogFrom("dog-es-2", "es"));

        serviceCase.updateEvent("event-1", command, "user-1", true);

        // 2 competitors -> E; both from ES (case-insensitive) -> not international -> "E".
        assertThat(recordedRank()).isEqualTo("E");
    }

    @Test
    void computes_rank_score_within_the_configuration_band_and_derives_rank_from_it() {
        // CPC_COBS band is [100, 200]; two competitors -> E tier; one from FR while the event is in ES ->
        // international. score = 100 + round(1/5 * 0.9 * 100) + round(0.1 * 100) = 100 + 18 + 10 = 128, "E+".
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "CPC_COBS.V0", 1735689600000L,
                ObdxAvgMethod.AVG,
                List.of(new UpdateObdxEventCommand.CompetitorCommand("dog-es", 1, null, false, false),
                        new UpdateObdxEventCommand.CompetitorCommand("dog-fr", 2, null, false, false)),
                List.of(), List.of(), List.of());
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competitionInCountry("ES"));
        when(getDogPersistencePort.getDog("dog-es")).thenReturn(dogFrom("dog-es", "ES"));
        when(getDogPersistencePort.getDog("dog-fr")).thenReturn(dogFrom("dog-fr", "FR"));

        serviceCase.updateEvent("event-1", command, "user-1", true);

        ObdxEventInfoUpdated change = recordedChange();
        assertThat(change.rankScore()).isEqualTo(128);
        assertThat(change.rank()).isEqualTo("E+");
    }

    @Test
    void leaves_rank_score_null_when_the_configuration_has_no_band() {
        // "config-1" is not a known configuration, so no band -> null score, letter falls back to tier+intl.
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());

        serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true);

        ObdxEventInfoUpdated change = recordedChange();
        assertThat(change.rankScore()).isNull();
        assertThat(change.rank()).isEqualTo("E");
    }

    private String recordedRank() {
        return recordedChange().rank();
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
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null, null);
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", 4102444800000L, Long.MAX_VALUE,
                0L, 0L, null, List.of(event));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", country, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    private Dog dogFrom(String id, String country) {
        return new Dog(id, "id-" + id, "breed", "Rex", "img", "owner-1", "handler-1", "creator-1", country, "team",
                Sex.FEMALE, 55, null, 0L, 0L, null);
    }
}
