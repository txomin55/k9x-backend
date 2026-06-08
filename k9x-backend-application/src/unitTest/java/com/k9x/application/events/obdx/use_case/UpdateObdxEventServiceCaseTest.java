package com.k9x.application.events.obdx.use_case;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventConfigurationIdRequiredException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.exceptions.ObdxCollectorNotFoundException;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.port.UpdateObdxEventPersistencePort;
import com.k9x.application.events.obdx.use_case.command.UpdateObdxEventCommand;
import com.k9x.application.events.obdx.use_case.dto.ObdxClassificationConfigDTO;
import com.k9x.application.users.port.GetUserInfoPersistencePort;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.disciplines.ClassificationCacheEvictStrategy;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateObdxEventServiceCaseTest {

    private static final UpdateObdxEventCommand VALID_COMMAND = new UpdateObdxEventCommand(
            "Event 1", "config-1", 1735689600000L, List.of(), List.of(), List.of()
    );
    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;
    @Mock
    private UpdateObdxEventPersistencePort updateObdxEventPersistencePort;
    @Mock
    private GetObdxClassificationConfigPort getObdxClassificationConfigPort;
    @Mock
    private GetUserInfoPersistencePort getUserInfoPersistencePort;
    private UpdateObdxEventServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateObdxEventServiceCase(getCompetitionPersistencePort, updateObdxEventPersistencePort,
                getObdxClassificationConfigPort, getUserInfoPersistencePort);
    }

    private Event event(Long deletedAt, String creator) {
        return new Event("event-1", null, null, "Event 1", "stage-1", creator, null, 0L, 0L, deletedAt,
                ObdxAvgMethod.MID_AVG, List.of(), List.of(), List.of(), List.of());
    }

    private Competition competition(Event event) {
        Stage stage = new Stage("stage-1", "Stage 1", "comp-1", "user-1", 0L, Long.MAX_VALUE, 0L, 0L, null, List.of(event));
        return new Competition("comp-1", "WC", "user-1", "Org", null, null, null, null, null,
                0L, 0L, null, List.of(stage));
    }

    private void givenEvent(Event event) {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn("comp-1");
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition(event));
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, updateObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_configuration_id_is_null() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", null, 1735689600000L, List.of(), List.of(), List.of());

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(EventConfigurationIdRequiredException.class);

        verifyNoInteractions(getCompetitionPersistencePort, updateObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_configuration_id_is_blank() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "  ", 1735689600000L, List.of(), List.of(), List.of());

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(EventConfigurationIdRequiredException.class);

        verifyNoInteractions(getCompetitionPersistencePort, updateObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getCompetitionPersistencePort.competitionIdByEvent("event-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(updateObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        givenEvent(event(1700000000000L, "user-1"));

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(updateObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_event_creator() {
        givenEvent(event(null, "other-user"));

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(updateObdxEventPersistencePort);
    }

    @Test
    void throws_exception_when_collector_email_does_not_exist() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, List.of(), List.of(),
                List.of(new UpdateObdxEventCommand.JudgeCommand("judge-1", "missing@k9x.com")));
        givenEvent(event(null, "user-1"));
        when(getUserInfoPersistencePort.findById("missing@k9x.com")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateEvent("event-1", command, "user-1", true))
                .isInstanceOf(ObdxCollectorNotFoundException.class);

        verifyNoInteractions(updateObdxEventPersistencePort);
    }

    @Test
    void updates_event_when_collector_email_exists() {
        UpdateObdxEventCommand command = new UpdateObdxEventCommand("Event 1", "config-1", 1735689600000L, List.of(), List.of(),
                List.of(new UpdateObdxEventCommand.JudgeCommand("judge-1", "collector@k9x.com")));
        ObdxClassificationConfigDTO config = new ObdxClassificationConfigDTO(ClassificationCacheEvictStrategy.OBDX, null, Map.of(), List.of(), List.of());
        givenEvent(event(null, "user-1"));
        when(getUserInfoPersistencePort.findById("collector@k9x.com"))
                .thenReturn(new UserInfoDTO("collector@k9x.com", "collector@k9x.com", null, false));
        when(getObdxClassificationConfigPort.getConfig("config-1")).thenReturn(config);

        serviceCase.updateEvent("event-1", command, "user-1", true);

        verify(updateObdxEventPersistencePort).updateEvent(eq("event-1"), any());
    }

    @Test
    void updates_event_when_all_validations_pass() {
        ObdxClassificationConfigDTO config = new ObdxClassificationConfigDTO(ClassificationCacheEvictStrategy.OBDX, null, Map.of(), List.of(), List.of());
        givenEvent(event(null, "user-1"));
        when(getObdxClassificationConfigPort.getConfig("config-1")).thenReturn(config);

        serviceCase.updateEvent("event-1", VALID_COMMAND, "user-1", true);

        verify(updateObdxEventPersistencePort).updateEvent(eq("event-1"), any());
    }
}
