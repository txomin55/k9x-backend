package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.GeoCoordinatesPort;
import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.application.competitions.use_case.command.UpdateCompetitionCommand;
import com.k9x.application.competitions.use_case.dto.Coordinates;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.competitions.aggregates.CompetitionSource;
import com.k9x.domain.competitions.exceptions.CompetitionNotFoundException;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCompetitionServiceCaseTest {

    @Mock
    private GetCompetitionPersistencePort getCompetitionPersistencePort;

    @Mock
    private GeoCoordinatesPort geoCoordinatesPort;

    @Mock
    private SaveCompetitionPersistencePort saveCompetitionPersistencePort;

    private UpdateCompetitionServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateCompetitionServiceCase(getCompetitionPersistencePort, geoCoordinatesPort, saveCompetitionPersistencePort);
    }

    private CompetitionSnapshot competition(String creator) {
        return new CompetitionSnapshot("comp-1", "World Cup", creator, "Org", "ES", "desc", "addr",
                null, null, CompetitionSource.API, null, 0L, 0L, null, List.of());
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.updateCompetition("comp-1", new UpdateCompetitionCommand("Name", "Desc", "ES", "Address"), "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionPersistencePort, geoCoordinatesPort, saveCompetitionPersistencePort);
    }

    @Test
    void throws_exception_when_competition_does_not_exist() {
        when(geoCoordinatesPort.getCoordinates("Address")).thenReturn(new Coordinates(0.0, 0.0));
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.updateCompetition("comp-1", new UpdateCompetitionCommand("Name", "Desc", "ES", "Address"), "user-1", true))
                .isInstanceOf(CompetitionNotFoundException.class);

        verifyNoInteractions(saveCompetitionPersistencePort);
    }

    @Test
    void saves_aggregate_when_all_validations_pass() {
        when(geoCoordinatesPort.getCoordinates("Address")).thenReturn(new Coordinates(40.4168, -3.7038));
        when(getCompetitionPersistencePort.getCompetition("comp-1")).thenReturn(competition("user-1"));

        serviceCase.updateCompetition("comp-1", new UpdateCompetitionCommand("Name", "Desc", "ES", "Address"), "user-1", true);

        verify(saveCompetitionPersistencePort).save(any());
    }

}
