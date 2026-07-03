package com.k9x.infrastructure.out.enums.countries;

import com.k9x.application.countries.use_case.dto.CountryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CountryEnumAdapterTest {

    private CountryEnumAdapter adapter;

    @BeforeEach
    void setUp() {
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(anyString(), isNull(), anyString(), any()))
                .thenAnswer(invocation -> invocation.getArgument(2));
        adapter = new CountryEnumAdapter(messageSource);
    }

    @Test
    void returns_one_entry_per_enum_constant() {
        List<CountryDTO> result = adapter.getCountries();

        assertThat(result).hasSize(Country.values().length);
    }

    @Test
    void id_matches_enum_constant_name() {
        List<CountryDTO> result = adapter.getCountries();

        assertThat(result).extracting(CountryDTO::id)
                .containsExactlyInAnyOrder("ES", "PT", "FR", "IT", "DE", "GB", "NL", "BE");
    }

    @Test
    void name_falls_back_to_enum_name_when_no_translation() {
        List<CountryDTO> result = adapter.getCountries();

        assertThat(result).allSatisfy(country -> assertThat(country.name()).isEqualTo(country.id()));
    }
}
