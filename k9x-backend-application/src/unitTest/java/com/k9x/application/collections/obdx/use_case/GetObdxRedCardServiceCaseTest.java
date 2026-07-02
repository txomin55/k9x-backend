package com.k9x.application.collections.obdx.use_case;

import com.k9x.application.collections.obdx.port.GetObdxRedCardPersistencePort;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxRedCardDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetObdxRedCardServiceCaseTest {

    @Mock
    private GetObdxRedCardPersistencePort getObdxRedCardPersistencePort;
    private GetObdxRedCardServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetObdxRedCardServiceCase(getObdxRedCardPersistencePort);
    }

    @Test
    void returns_red_card_from_persistence() {
        FetchObdxRedCardDTO redCard = new FetchObdxRedCardDTO("OBDX_FCI_GRADE_3.1_V0", "judge-1", "Judge One", 1000L);
        when(getObdxRedCardPersistencePort.getRedCard("event-1", "dog-1")).thenReturn(redCard);

        assertThat(serviceCase.getRedCard("event-1", "dog-1")).isEqualTo(redCard);
    }

    @Test
    void returns_null_when_no_red_card_is_registered() {
        when(getObdxRedCardPersistencePort.getRedCard("event-1", "dog-1")).thenReturn(null);

        assertThat(serviceCase.getRedCard("event-1", "dog-1")).isNull();
    }
}
