package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.GetCompetitionListPersistencePort;
import com.k9x.application.competitions.use_case.dto.FetchCompetitionDTO;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCompetitionListServiceCaseTest {

    @Mock
    private GetCompetitionListPersistencePort getCompetitionListPersistencePort;

    private GetCompetitionListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetCompetitionListServiceCase(getCompetitionListPersistencePort);
    }

    private Competition competition(String id, List<Stage> stages) {
        return new Competition(id, "World Cup", "user-1", "Org", "ES", "desc", "Calle Mayor 1",
                null, null, 0L, 0L, null, stages);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.getCompetitions("user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionListPersistencePort);
    }

    @Test
    void maps_empty_competition_as_created() {
        when(getCompetitionListPersistencePort.getCompetitions("user-1"))
                .thenReturn(List.of(competition("comp-1", List.of())));

        List<FetchCompetitionDTO> result = serviceCase.getCompetitions("user-1", true);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("comp-1");
        assertThat(result.getFirst().status()).isEqualTo("CREATED");
        assertThat(result.getFirst().stages()).isEmpty();
        verify(getCompetitionListPersistencePort).getCompetitions("user-1");
    }

    @Test
    void maps_competition_with_finished_stage_as_finished() {
        // dateTo = 0L (1970) is strictly before today's UTC day -> FINISHED stage -> FINISHED competition.
        Stage finishedStage = new Stage("stage-1", "Stage 1", "comp-1", "user-1",
                0L, 0L, 0L, 0L, null, List.of());
        when(getCompetitionListPersistencePort.getCompetitions("user-1"))
                .thenReturn(List.of(competition("comp-1", List.of(finishedStage))));

        List<FetchCompetitionDTO> result = serviceCase.getCompetitions("user-1", true);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo("FINISHED");
        assertThat(result.getFirst().stages()).hasSize(1);
    }
}
