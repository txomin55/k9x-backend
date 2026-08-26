package com.k9x.application.notifications.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.notifications.exceptions.NotificationEventsRequiredException;
import com.k9x.application.notifications.port.GetEventRecipientsPersistencePort;
import com.k9x.application.notifications.port.PushNotifier;
import com.k9x.application.notifications.port.SaveEventNotificationPersistencePort;
import com.k9x.application.notifications.port.SaveNotificationPersistencePort;
import com.k9x.application.notifications.port.payload.SaveEventNotificationPersistencePayload;
import com.k9x.application.notifications.port.payload.SaveNotificationPersistencePayload;
import com.k9x.application.notifications.use_case.command.CreateStageNotificationCommand;
import com.k9x.application.notifications.valueobjects.NotificationType;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.competitions.aggregates.CompetitionSource;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.exceptions.EventAlreadyDeletedException;
import com.k9x.domain.events.exceptions.EventFinishedException;
import com.k9x.domain.events.exceptions.EventNotFoundException;
import com.k9x.domain.events.exceptions.EventNotInStageException;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import com.k9x.domain.stages.exceptions.StageAlreadyDeletedException;
import com.k9x.domain.stages.exceptions.StageFinishedException;
import com.k9x.domain.stages.exceptions.StageNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateStageNotificationsServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private SaveEventNotificationPersistencePort saveEventNotificationPersistencePort;

    @Mock
    private GetEventRecipientsPersistencePort getEventRecipientsPersistencePort;

    @Mock
    private SaveNotificationPersistencePort saveNotificationPersistencePort;

    @Mock
    private PushNotifier pushNotifier;

    private CreateStageNotificationsServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new CreateStageNotificationsServiceCase(getCompetitionPersistencePort,
                saveEventNotificationPersistencePort, getEventRecipientsPersistencePort,
                saveNotificationPersistencePort, pushNotifier);
    }

    private EventSnapshot event(String id, String stageId, String creator, Long deletedAt) {
        return new EventSnapshot(id, null, null, "Event " + id, stageId, creator, Long.MAX_VALUE, 0L, 0L,
                deletedAt, ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of(), List.of(), null, null, null);
    }

    /** Stage 1 owns event-1 and event-2, both created by user-1. Stage 2 owns event-3. */
    private CompetitionSnapshot competition() {
        StageSnapshot stageOne = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE,
                Long.MAX_VALUE, 0L, 0L, null,
                List.of(event("event-1", "stage-1", "user-1", null), event("event-2", "stage-1", "user-1", null)));
        StageSnapshot stageTwo = new StageSnapshot("stage-2", "Stage 2", "comp-1", "user-1", Long.MAX_VALUE,
                Long.MAX_VALUE, 0L, 0L, null, List.of(event("event-3", "stage-2", "user-1", null)));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                CompetitionSource.API, 0L, 0L, null, List.of(stageOne, stageTwo));
    }

    /** Stage 1 as a stage whose last day is long past, so its events are FINISHED. */
    private CompetitionSnapshot competitionWithFinishedStage() {
        StageSnapshot finishedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", 0L, 0L, 0L, 0L,
                null, List.of(event("event-1", "stage-1", "user-1", null)));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                CompetitionSource.API, 0L, 0L, null, List.of(finishedStage));
    }

    /**
     * A running stage (its last day is far off) whose event-1 is already FINISHED — its only competitor is
     * flagged as not competing, so every competitor is settled — while event-2 is still open, which keeps
     * the stage itself from being FINISHED.
     */
    private CompetitionSnapshot competitionWithFinishedEvent() {
        EventCompetitor notCompeting = new EventCompetitor("dog-1", "Rex", "owner-1", "handler-1", null, null,
                null, null, null, null, null, null, null, true, null, null, null, null, null);
        EventSnapshot finishedEvent = new EventSnapshot("event-1", null, null, "Event event-1", "stage-1",
                "user-1", Long.MAX_VALUE, 0L, 0L, null, ObdxAvgMethod.MID_AVG, List.of(notCompeting),
                List.of(), List.of(), List.of(), List.of(), null, null, null);
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE,
                Long.MAX_VALUE, 0L, 0L, null,
                List.of(finishedEvent, event("event-2", "stage-1", "user-1", null)));
        return new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                CompetitionSource.API, 0L, 0L, null, List.of(stage));
    }

    private void stageOneExists() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition());
    }

    private List<CreateStageNotificationCommand> announcement(String... eventIds) {
        return List.of(new CreateStageNotificationCommand(List.of(eventIds), "Ceremony delayed"));
    }

    private void verifyNothingWritten() {
        verifyNoInteractions(saveEventNotificationPersistencePort, saveNotificationPersistencePort, pushNotifier);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.createStageNotifications(
                "stage-1", announcement("event-1"), "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNothingWritten();
    }

    @Test
    void throws_exception_when_stage_not_found() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.createStageNotifications(
                "stage-1", announcement("event-1"), "user-1", true))
                .isInstanceOf(StageNotFoundException.class);

        verifyNothingWritten();
    }

    @Test
    void throws_exception_when_an_announcement_addresses_no_event() {
        stageOneExists();

        assertThatThrownBy(() -> serviceCase.createStageNotifications("stage-1", List.of(
                new CreateStageNotificationCommand(List.of("event-1"), "First"),
                new CreateStageNotificationCommand(List.of(), "Second")), "user-1", true))
                .isInstanceOf(NotificationEventsRequiredException.class);

        verifyNothingWritten();
    }

    @Test
    void throws_exception_when_an_announcement_carries_null_events() {
        stageOneExists();

        assertThatThrownBy(() -> serviceCase.createStageNotifications("stage-1",
                List.of(new CreateStageNotificationCommand(null, "First")), "user-1", true))
                .isInstanceOf(NotificationEventsRequiredException.class);

        verifyNothingWritten();
    }

    @Test
    void throws_exception_when_event_belongs_to_another_stage() {
        stageOneExists();

        assertThatThrownBy(() -> serviceCase.createStageNotifications(
                "stage-1", announcement("event-1", "event-3"), "user-1", true))
                .isInstanceOf(EventNotInStageException.class);

        verifyNothingWritten();
    }

    @Test
    void throws_exception_when_event_does_not_exist() {
        stageOneExists();

        assertThatThrownBy(() -> serviceCase.createStageNotifications(
                "stage-1", announcement("event-unknown"), "user-1", true))
                .isInstanceOf(EventNotFoundException.class);

        verifyNothingWritten();
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE,
                Long.MAX_VALUE, 0L, 0L, null, List.of(event("event-1", "stage-1", "user-1", 123L)));
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(
                new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                        CompetitionSource.API, 0L, 0L, null, List.of(stage)));

        assertThatThrownBy(() -> serviceCase.createStageNotifications(
                "stage-1", announcement("event-1"), "user-1", true))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNothingWritten();
    }

    @Test
    void throws_exception_when_user_did_not_create_the_event() {
        stageOneExists();

        assertThatThrownBy(() -> serviceCase.createStageNotifications(
                "stage-1", announcement("event-1"), "other-user", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNothingWritten();
    }

    @Test
    void throws_exception_when_the_stage_has_already_finished() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competitionWithFinishedStage());

        assertThatThrownBy(() -> serviceCase.createStageNotifications(
                "stage-1", announcement("event-1"), "user-1", true))
                .isInstanceOf(StageFinishedException.class);

        verifyNothingWritten();
    }

    @Test
    void throws_exception_when_the_stage_is_deleted() {
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1", Long.MAX_VALUE,
                Long.MAX_VALUE, 0L, 0L, 123L, List.of(event("event-1", "stage-1", "user-1", null)));
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(
                new CompetitionSnapshot("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                        CompetitionSource.API, 0L, 0L, null, List.of(stage)));

        assertThatThrownBy(() -> serviceCase.createStageNotifications(
                "stage-1", announcement("event-1"), "user-1", true))
                .isInstanceOf(StageAlreadyDeletedException.class);

        verifyNothingWritten();
    }

    @Test
    void throws_exception_when_the_event_has_already_finished_within_a_running_stage() {
        when(getCompetitionPersistencePort.competitionIdByStage("stage-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1"))
                .thenReturn(competitionWithFinishedEvent());

        assertThatThrownBy(() -> serviceCase.createStageNotifications(
                "stage-1", announcement("event-1"), "user-1", true))
                .isInstanceOf(EventFinishedException.class);

        verifyNothingWritten();
    }

    @Test
    void notifies_every_recipient_once_when_all_validations_pass() {
        stageOneExists();
        when(getEventRecipientsPersistencePort.getRecipientIds(List.of("event-1", "event-2")))
                .thenReturn(Set.of("owner-1", "owner-2"));

        serviceCase.createStageNotifications("stage-1", announcement("event-1", "event-2"), "user-1", true);

        verify(saveEventNotificationPersistencePort).save(any());
        verify(saveNotificationPersistencePort, times(2)).save(any());
        verify(pushNotifier).deliver(eq("owner-1"), any());
        verify(pushNotifier).deliver(eq("owner-2"), any());
    }

    @Test
    void does_not_notify_the_organizer_that_created_the_announcement() {
        stageOneExists();
        when(getEventRecipientsPersistencePort.getRecipientIds(List.of("event-1")))
                .thenReturn(Set.of("user-1", "owner-2"));

        serviceCase.createStageNotifications("stage-1", announcement("event-1"), "user-1", true);

        ArgumentCaptor<SaveNotificationPersistencePayload> captor =
                ArgumentCaptor.forClass(SaveNotificationPersistencePayload.class);
        verify(saveNotificationPersistencePort).save(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo("owner-2");
        verify(pushNotifier).deliver(eq("owner-2"), any());
        verify(pushNotifier, never()).deliver(eq("user-1"), any());
    }

    @Test
    void stores_the_announcement_with_every_event_it_applies_to() {
        stageOneExists();
        when(getEventRecipientsPersistencePort.getRecipientIds(anyList())).thenReturn(Set.of("owner-1"));

        serviceCase.createStageNotifications("stage-1", announcement("event-1", "event-2"), "user-1", true);

        ArgumentCaptor<SaveEventNotificationPersistencePayload> captor =
                ArgumentCaptor.forClass(SaveEventNotificationPersistencePayload.class);
        verify(saveEventNotificationPersistencePort).save(captor.capture());
        assertThat(captor.getValue().eventIds()).containsExactly("event-1", "event-2");
        assertThat(captor.getValue().content()).isEqualTo("Ceremony delayed");
        assertThat(captor.getValue().timestamp()).isPositive();
    }

    @Test
    void inbox_metadata_carries_only_the_stage_and_the_content() {
        stageOneExists();
        when(getEventRecipientsPersistencePort.getRecipientIds(anyList())).thenReturn(Set.of("owner-1"));

        serviceCase.createStageNotifications("stage-1", announcement("event-1"), "user-1", true);

        ArgumentCaptor<SaveNotificationPersistencePayload> captor =
                ArgumentCaptor.forClass(SaveNotificationPersistencePayload.class);
        verify(saveNotificationPersistencePort).save(captor.capture());
        SaveNotificationPersistencePayload saved = captor.getValue();
        assertThat(saved.userId()).isEqualTo("owner-1");
        assertThat(saved.type()).isEqualTo(NotificationType.EVENT_NOTIFICATION);
        assertThat(saved.metadata()).containsOnlyKeys("stage_id", "stage_name", "content");
        assertThat(saved.metadata()).containsEntry("stage_id", "stage-1");
        assertThat(saved.metadata()).containsEntry("stage_name", "Stage 1");
        assertThat(saved.metadata()).containsEntry("content", "Ceremony delayed");
    }

    @Test
    void writes_one_announcement_per_command() {
        stageOneExists();
        when(getEventRecipientsPersistencePort.getRecipientIds(anyList())).thenReturn(Set.of("owner-1"));

        serviceCase.createStageNotifications("stage-1", List.of(
                new CreateStageNotificationCommand(List.of("event-1"), "First"),
                new CreateStageNotificationCommand(List.of("event-2"), "Second")), "user-1", true);

        verify(saveEventNotificationPersistencePort, times(2)).save(any());
        verify(saveNotificationPersistencePort, times(2)).save(any());
        verify(pushNotifier, times(2)).deliver(any(), any());
    }

    @Test
    void does_nothing_when_there_are_no_announcements() {
        serviceCase.createStageNotifications("stage-1", List.of(), "user-1", true);

        verifyNoInteractions(getCompetitionPersistencePort);
        verifyNothingWritten();
    }
}
