package com.k9x.application.countries.use_case;

import com.k9x.application.countries.port.GetCountryListPort;
import com.k9x.application.countries.use_case.dto.CountryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCountryListServiceCaseTest {

    @Mock
    GetCountryListPort getCountryListPort;

    private GetCountryListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetCountryListServiceCase(getCountryListPort);
    }

    @Test
    void returns_countries_from_port() {
        List<CountryDTO> countries = List.of(new CountryDTO("ES", "España"));
        when(getCountryListPort.getCountries()).thenReturn(countries);

        assertThat(serviceCase.getCountries()).isEqualTo(countries);
    }
}
