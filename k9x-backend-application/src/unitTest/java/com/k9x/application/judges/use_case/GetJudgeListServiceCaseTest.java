package com.k9x.application.judges.use_case;

import com.k9x.application.judges.dto.JudgeDTO;
import com.k9x.application.judges.port.GetJudgeListPersistencePort;
import com.k9x.domain.aggregates.judges.Judge;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetJudgeListServiceCaseTest {

    @Mock
    private GetJudgeListPersistencePort getJudgeListPersistencePort;

    private GetJudgeListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetJudgeListServiceCase(getJudgeListPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.getJudges("user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);
    }

    @Test
    void fetches_judges_by_user_id_when_organizer() {
        when(getJudgeListPersistencePort.getJudges("user-1")).thenReturn(List.of());

        serviceCase.getJudges("user-1", true);

        verify(getJudgeListPersistencePort).getJudges("user-1");
    }

    @Test
    void maps_judge_fields_to_dto_correctly() {
        Judge judge = new Judge("id-1", "Rex", "user-1", 0L, 0L, 0L);
        when(getJudgeListPersistencePort.getJudges("user-1")).thenReturn(List.of(judge));

        List<JudgeDTO> result = serviceCase.getJudges("user-1", true);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("id-1");
        assertThat(result.getFirst().name()).isEqualTo("Rex");
    }
}
