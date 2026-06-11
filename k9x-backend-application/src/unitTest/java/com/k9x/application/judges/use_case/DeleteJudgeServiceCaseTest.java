package com.k9x.application.judges.use_case;

import com.k9x.application.judges.exceptions.JudgeAlreadyDeletedException;
import com.k9x.application.judges.exceptions.JudgeNotFoundException;
import com.k9x.application.judges.port.DeleteJudgePersistencePort;
import com.k9x.application.judges.port.GetJudgePersistencePort;
import com.k9x.domain.judges.aggregates.Judge;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteJudgeServiceCaseTest {

    @Mock
    private GetJudgePersistencePort getJudgePersistencePort;

    @Mock
    private DeleteJudgePersistencePort deleteJudgePersistencePort;

    private DeleteJudgeServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new DeleteJudgeServiceCase(getJudgePersistencePort, deleteJudgePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.deleteJudge("judge-1", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getJudgePersistencePort, deleteJudgePersistencePort);
    }

    @Test
    void throws_exception_when_judge_not_found() {
        when(getJudgePersistencePort.getJudge("judge-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.deleteJudge("judge-1", "user-1", true))
                .isInstanceOf(JudgeNotFoundException.class);

        verifyNoInteractions(deleteJudgePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_creator() {
        Judge judge = new Judge("judge-1", "Rex", "other-user", 0L, 0L, null);
        when(getJudgePersistencePort.getJudge("judge-1")).thenReturn(judge);

        assertThatThrownBy(() -> serviceCase.deleteJudge("judge-1", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(deleteJudgePersistencePort);
    }

    @Test
    void throws_exception_when_judge_already_deleted() {
        Judge judge = new Judge("judge-1", "Rex", "user-1", 0L, 0L, 1000L);
        when(getJudgePersistencePort.getJudge("judge-1")).thenReturn(judge);

        assertThatThrownBy(() -> serviceCase.deleteJudge("judge-1", "user-1", true))
                .isInstanceOf(JudgeAlreadyDeletedException.class);

        verifyNoInteractions(deleteJudgePersistencePort);
    }

    @Test
    void deletes_judge_when_all_validations_pass() {
        Judge judge = new Judge("judge-1", "Rex", "user-1", 0L, 0L, null);
        when(getJudgePersistencePort.getJudge("judge-1")).thenReturn(judge);

        serviceCase.deleteJudge("judge-1", "user-1", true);

        verify(deleteJudgePersistencePort).deleteJudge(eq("judge-1"), anyLong());
    }
}
