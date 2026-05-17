package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.dto.Coordinates;
import com.k9x.application.competitions.exceptions.CompetitionAlreadyDeletedException;
import com.k9x.application.competitions.exceptions.CompetitionNotFoundException;
import com.k9x.application.competitions.port.GeoCoordinatesPort;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.UpdateCompetitionPersistencePort;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCompetitionServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private GeoCoordinatesPort geoCoordinatesPort;

    @Mock
    private UpdateCompetitionPersistencePort updateCompetitionPersistencePort;

    private UpdateCompetitionServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateCompetitionServiceCase(getCompetitionPersistencePort, geoCoordinatesPort, updateCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.updateCompetition("comp-1", "Name", "Desc", "Address", "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, geoCoordinatesPort, updateCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_competition_does_not_exist() {
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateCompetition("comp-1", "Name", "Desc", "Address", "user-1", true))
                .isInstanceOf(CompetitionNotFoundException.class);

        verifyNoInteractions(geoCoordinatesPort, updateCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_competition_is_deleted() {
        Competition competition = new Competition("comp-1", "World Cup", "user-1", 0L, 0L, 1700000000000L);
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition);

        assertThatThrownBy(() -> serviceCase.updateCompetition("comp-1", "Name", "Desc", "Address", "user-1", true))
                .isInstanceOf(CompetitionAlreadyDeletedException.class);

        verifyNoInteractions(geoCoordinatesPort, updateCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_creator() {
        Competition competition = new Competition("comp-1", "World Cup", "other-user", 0L, 0L, null);
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition);

        assertThatThrownBy(() -> serviceCase.updateCompetition("comp-1", "Name", "Desc", "Address", "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(geoCoordinatesPort, updateCompetitionPersistencePort);
    }

    @Test
    void updates_competition_when_all_validations_pass() {
        Competition competition = new Competition("comp-1", "World Cup", "user-1", 0L, 0L, null);
        Coordinates coordinates = new Coordinates(40.4168, -3.7038);
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition);
        when(geoCoordinatesPort.getCoordinates("Address")).thenReturn(coordinates);

        serviceCase.updateCompetition("comp-1", "Name", "Desc", "Address", "user-1", true);

        verify(updateCompetitionPersistencePort).updateCompetition(eq("comp-1"), eq("Name"), eq("Desc"),
                eq("Address"), eq(40.4168), eq(-3.7038), anyLong());
    }
}
