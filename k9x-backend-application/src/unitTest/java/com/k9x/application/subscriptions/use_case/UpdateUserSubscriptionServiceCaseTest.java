package com.k9x.application.subscriptions.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.subscriptions.port.CreateUserSubscriptionsPersistencePort;
import com.k9x.application.subscriptions.port.UpdateUserSubscriptionPersistencePort;
import com.k9x.application.subscriptions.port.payload.UpdateUserSubscriptionPersistencePayload;
import com.k9x.application.subscriptions.use_case.command.UpdateUserSubscriptionCommand;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.exceptions.EventFinishedException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.subscriptions.SubscriptionKind;
import com.k9x.domain.subscriptions.exceptions.SubscriptionKindNotSupportedException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserSubscriptionServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private CreateUserSubscriptionsPersistencePort createUserSubscriptionsPersistencePort;

    @Mock
    private UpdateUserSubscriptionPersistencePort updateUserSubscriptionPersistencePort;

    private UpdateUserSubscriptionServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateUserSubscriptionServiceCase(getCompetitionPersistencePort,
                createUserSubscriptionsPersistencePort, updateUserSubscriptionPersistencePort);
    }

    /** A competition whose two events belong to a stage that ends on the given day. */
    private CompetitionSnapshot competition(long stageDateTo) {
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "owner@test.com", stageDateTo,
                stageDateTo, 0L, 0L, null, List.of(event("event-1"), event("event-2")));
        return new CompetitionSnapshot("comp-1", "WC", "owner@test.com", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    private EventSnapshot event(String id) {
        return new EventSnapshot(id, null, null, "Event " + id, "stage-1", "owner@test.com", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null, null, null);
    }

    private void eventBelongsToCompetition(long stageDateTo) {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(stageDateTo));
    }

    @Test
    void throws_exception_when_kind_is_not_supported() {
        UpdateUserSubscriptionCommand command = new UpdateUserSubscriptionCommand("STAGE", List.of("stage-1"), true);

        assertThatThrownBy(() -> serviceCase.updateUserSubscription(command, "user@test.com"))
                .isInstanceOf(SubscriptionKindNotSupportedException.class);

        verifyNoInteractions(createUserSubscriptionsPersistencePort, updateUserSubscriptionPersistencePort);
    }

    @Test
    void throws_exception_when_kind_is_null() {
        UpdateUserSubscriptionCommand command = new UpdateUserSubscriptionCommand(null, List.of("event-1"), true);

        assertThatThrownBy(() -> serviceCase.updateUserSubscription(command, "user@test.com"))
                .isInstanceOf(SubscriptionKindNotSupportedException.class);

        verifyNoInteractions(createUserSubscriptionsPersistencePort, updateUserSubscriptionPersistencePort);
    }

    @Test
    void throws_exception_when_subscribing_to_an_unknown_event() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);
        UpdateUserSubscriptionCommand command = new UpdateUserSubscriptionCommand("EVENT", List.of("event-1"), true);

        assertThatThrownBy(() -> serviceCase.updateUserSubscription(command, "user@test.com"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(createUserSubscriptionsPersistencePort, updateUserSubscriptionPersistencePort);
    }

    @Test
    void throws_exception_when_subscribing_to_a_finished_event() {
        eventBelongsToCompetition(0L);
        UpdateUserSubscriptionCommand command = new UpdateUserSubscriptionCommand("EVENT", List.of("event-1"), true);

        assertThatThrownBy(() -> serviceCase.updateUserSubscription(command, "user@test.com"))
                .isInstanceOf(EventFinishedException.class);

        verifyNoInteractions(createUserSubscriptionsPersistencePort, updateUserSubscriptionPersistencePort);
    }

    @Test
    void unsubscribes_from_a_finished_event_without_validating_it() {
        serviceCase.updateUserSubscription(
                new UpdateUserSubscriptionCommand("EVENT", List.of("event-1"), false), "user@test.com");

        verifyNoInteractions(getCompetitionPersistencePort);
        verify(updateUserSubscriptionPersistencePort).updateUserSubscription(any());
    }

    @Test
    void does_nothing_when_no_ids_are_sent() {
        serviceCase.updateUserSubscription(
                new UpdateUserSubscriptionCommand("EVENT", List.of(), true), "user@test.com");

        verifyNoInteractions(getCompetitionPersistencePort, createUserSubscriptionsPersistencePort,
                updateUserSubscriptionPersistencePort);
    }

    @Test
    void subscribes_to_every_event_of_the_set_in_one_write() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.competitionIdByEvent("event-2")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(Long.MAX_VALUE));

        serviceCase.updateUserSubscription(
                new UpdateUserSubscriptionCommand("EVENT", List.of("event-1", "event-2"), true), "user@test.com");

        assertThat(capturedPayload().targetIds()).containsExactly("event-1", "event-2");
    }

    @Test
    void rejects_the_whole_set_when_one_of_its_events_has_finished() {
        eventBelongsToCompetition(0L);

        assertThatThrownBy(() -> serviceCase.updateUserSubscription(
                new UpdateUserSubscriptionCommand("EVENT", List.of("event-1", "event-2"), true), "user@test.com"))
                .isInstanceOf(EventFinishedException.class);

        verifyNoInteractions(createUserSubscriptionsPersistencePort, updateUserSubscriptionPersistencePort);
    }

    @Test
    void creates_the_subscriptions_record_before_updating_it() {
        eventBelongsToCompetition(Long.MAX_VALUE);

        serviceCase.updateUserSubscription(
                new UpdateUserSubscriptionCommand("EVENT", List.of("event-1"), true), "user@test.com");

        verify(createUserSubscriptionsPersistencePort).createUserSubscriptions("user@test.com");
        verify(updateUserSubscriptionPersistencePort).updateUserSubscription(any());
    }

    @Test
    void sends_subscribe_payload_when_subscribe_is_true() {
        eventBelongsToCompetition(Long.MAX_VALUE);

        serviceCase.updateUserSubscription(
                new UpdateUserSubscriptionCommand("EVENT", List.of("event-1"), true), "user@test.com");

        assertThat(capturedPayload()).isEqualTo(new UpdateUserSubscriptionPersistencePayload(
                "user@test.com", SubscriptionKind.EVENT, List.of("event-1"), true));
    }

    @Test
    void sends_unsubscribe_payload_when_subscribe_is_false() {
        serviceCase.updateUserSubscription(
                new UpdateUserSubscriptionCommand("event", List.of("event-1"), false), "user@test.com");

        assertThat(capturedPayload()).isEqualTo(new UpdateUserSubscriptionPersistencePayload(
                "user@test.com", SubscriptionKind.EVENT, List.of("event-1"), false));
    }

    private UpdateUserSubscriptionPersistencePayload capturedPayload() {
        ArgumentCaptor<UpdateUserSubscriptionPersistencePayload> captor =
                ArgumentCaptor.forClass(UpdateUserSubscriptionPersistencePayload.class);
        verify(updateUserSubscriptionPersistencePort).updateUserSubscription(captor.capture());
        return captor.getValue();
    }
}
