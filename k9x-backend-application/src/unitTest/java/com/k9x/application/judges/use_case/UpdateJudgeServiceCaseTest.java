package com.k9x.application.judges.use_case;

import com.k9x.application.judges.exceptions.JudgeAlreadyDeletedException;
import com.k9x.application.judges.exceptions.JudgeNotFoundException;
import com.k9x.application.judges.port.GetJudgePersistencePort;
import com.k9x.application.judges.port.UpdateJudgePersistencePort;
import com.k9x.application.judges.use_case.command.UpdateJudgeCommand;
import com.k9x.domain.judges.aggregates.Judge;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateJudgeServiceCaseTest {

    @Mock
    private GetJudgePersistencePort getJudgePersistencePort;

    @Mock
    private UpdateJudgePersistencePort updateJudgePersistencePort;

    private UpdateJudgeServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateJudgeServiceCase(getJudgePersistencePort, updateJudgePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.updateJudge("judge-1", new UpdateJudgeCommand("Rex", "ES"), "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getJudgePersistencePort, updateJudgePersistencePort);
    }

    @Test
    void throws_exception_when_judge_not_found() {
        when(getJudgePersistencePort.getJudge("judge-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateJudge("judge-1", new UpdateJudgeCommand("Rex", "ES"), "user-1", true))
                .isInstanceOf(JudgeNotFoundException.class);

        verifyNoInteractions(updateJudgePersistencePort);
    }

    @Test
    void throws_exception_when_judge_is_deleted() {
        Judge judge = new Judge("judge-1", "Rex", "user-1", "ES", 0L, 0L, 1700000000000L);
        when(getJudgePersistencePort.getJudge("judge-1")).thenReturn(judge);

        assertThatThrownBy(() -> serviceCase.updateJudge("judge-1", new UpdateJudgeCommand("Rex", "ES"), "user-1", true))
                .isInstanceOf(JudgeAlreadyDeletedException.class);

        verifyNoInteractions(updateJudgePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_creator() {
        Judge judge = new Judge("judge-1", "Rex", "other-user", "ES", 0L, 0L, null);
        when(getJudgePersistencePort.getJudge("judge-1")).thenReturn(judge);

        assertThatThrownBy(() -> serviceCase.updateJudge("judge-1", new UpdateJudgeCommand("Rex", "ES"), "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(updateJudgePersistencePort);
    }

    @Test
    void updates_judge_when_all_validations_pass() {
        Judge judge = new Judge("judge-1", "Rex", "user-1", "ES", 0L, 0L, null);
        when(getJudgePersistencePort.getJudge("judge-1")).thenReturn(judge);

        serviceCase.updateJudge("judge-1", new UpdateJudgeCommand("NewName", "ES"), "user-1", true);

        verify(updateJudgePersistencePort).updateJudge(eq("judge-1"), any());
    }
}
