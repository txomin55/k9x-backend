package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.obdx.port.GetObdxYellowCardsPersistencePort;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxYellowCardDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetObdxYellowCardsServiceCaseTest {

    @Mock
    private GetObdxYellowCardsPersistencePort getObdxYellowCardsPersistencePort;
    private GetObdxYellowCardsServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetObdxYellowCardsServiceCase(getObdxYellowCardsPersistencePort);
    }

    @Test
    void returns_yellow_cards_from_persistence() {
        List<FetchObdxYellowCardDTO> yellowCards = List.of(
                new FetchObdxYellowCardDTO("OBDX_FCI_GRADE_3.1_V0", "judge-1", "Judge One", 1000L),
                new FetchObdxYellowCardDTO("OBDX_FCI_GRADE_3.1_V0", "judge-1", "Judge One", 2000L));
        when(getObdxYellowCardsPersistencePort.getYellowCards("event-1", "dog-1")).thenReturn(yellowCards);

        assertThat(serviceCase.getYellowCards("event-1", "dog-1")).isEqualTo(yellowCards);
    }
}
