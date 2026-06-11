package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CreateCompetitionServiceCaseTest {

    @Mock
    private SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    private CreateCompetitionServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new CreateCompetitionServiceCase(saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.createCompetition("comp-1", "World Cup", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void saves_aggregate_when_user_is_organizer() {
        serviceCase.createCompetition("comp-1", "World Cup", "user-1", true);

        verify(saveCompetitionPersistencePort).save(any());
    }
}
