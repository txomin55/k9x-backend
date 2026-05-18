package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.dto.FetchCompetitionDTO;
import com.k9x.application.competitions.port.GetCompetitionListPersistencePort;
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

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.getCompetitions("user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionListPersistencePort);
    }

    @Test
    void returns_competitions_for_creator() {
        List<FetchCompetitionDTO> competitions = List.of(
                new FetchCompetitionDTO("comp-1", "World Cup", "desc", "ES", "Calle Mayor 1", null, List.of())
        );
        when(getCompetitionListPersistencePort.getCompetitions("user-1")).thenReturn(competitions);

        List<FetchCompetitionDTO> result = serviceCase.getCompetitions("user-1", true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo("comp-1");
        assertThat(result.get(0).status()).isEqualTo("ACTIVE");
        verify(getCompetitionListPersistencePort).getCompetitions("user-1");
    }
}
