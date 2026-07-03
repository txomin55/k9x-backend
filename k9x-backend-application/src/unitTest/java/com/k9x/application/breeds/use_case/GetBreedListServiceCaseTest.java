package com.k9x.application.breeds.use_case;

import com.k9x.application.breeds.port.GetBreedListPort;
import com.k9x.application.breeds.use_case.dto.BreedDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBreedListServiceCaseTest {

    @Mock
    GetBreedListPort getBreedListPort;

    private GetBreedListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetBreedListServiceCase(getBreedListPort);
    }

    @Test
    void returns_breeds_from_port() {
        List<BreedDTO> breeds = List.of(new BreedDTO("BORDER_COLLIE", "Border Collie"));
        when(getBreedListPort.getBreeds()).thenReturn(breeds);

        assertThat(serviceCase.getBreeds()).isEqualTo(breeds);
    }
}
