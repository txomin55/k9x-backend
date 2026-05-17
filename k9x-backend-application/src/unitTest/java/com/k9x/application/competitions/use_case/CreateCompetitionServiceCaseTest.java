package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.CreateCompetitionPersistencePort;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCompetitionServiceCaseTest {

    @Mock
    private CreateCompetitionPersistencePort createCompetitionPersistencePort;

    private CreateCompetitionServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new CreateCompetitionServiceCase(createCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.createCompetition("comp-1", "World Cup", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(createCompetitionPersistencePort);
    }

    @Test
    void creates_competition_when_user_is_organizer() {
        serviceCase.createCompetition("comp-1", "World Cup", "user-1", true);

        verify(createCompetitionPersistencePort).createCompetition(eq("comp-1"), eq("World Cup"), eq("user-1"), anyLong());
    }
}
