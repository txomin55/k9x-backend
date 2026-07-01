package com.k9x.application.judges.use_case;

import com.k9x.application.judges.port.CreateJudgePersistencePort;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateJudgeServiceCaseTest {

    @Mock
    private CreateJudgePersistencePort createJudgePersistencePort;

    private CreateJudgeServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new CreateJudgeServiceCase(createJudgePersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.createJudge("judge-1", "Rex", "ES", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(createJudgePersistencePort);
    }

    @Test
    void creates_judge_when_user_is_organizer() {
        serviceCase.createJudge("judge-1", "Rex", "ES", "user-1", true);

        verify(createJudgePersistencePort).createJudge(eq("judge-1"), eq("Rex"), eq("ES"), eq("user-1"), anyLong());
    }
}
