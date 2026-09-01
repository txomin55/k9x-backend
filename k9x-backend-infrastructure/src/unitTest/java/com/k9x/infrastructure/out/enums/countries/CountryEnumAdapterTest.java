package com.k9x.infrastructure.out.enums.countries;

import com.k9x.application.countries.use_case.dto.CountryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.Collator;

import java.util.Arrays;
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
    void returns_one_entry_per_enum_constant_except_eu() {
        List<CountryDTO> result = adapter.getCountries();

        assertThat(result).hasSize(Country.values().length - 1);
    }

    @Test
    void id_matches_enum_constant_name() {
        List<CountryDTO> result = adapter.getCountries();

        assertThat(result).extracting(CountryDTO::id)
                .containsExactlyInAnyOrderElementsOf(Arrays.stream(Country.values())
                        .map(Enum::name).toList());
    }

    @Test
    void returns_countries_sorted_by_translated_name() {
        List<CountryDTO> result = adapter.getCountries();

        assertThat(result).extracting(CountryDTO::name)
                .isSortedAccordingTo(Collator.getInstance(LocaleContextHolder.getLocale()));
    }

    @Test
    void name_falls_back_to_enum_name_when_no_translation() {
        List<CountryDTO> result = adapter.getCountries();

        assertThat(result).allSatisfy(country -> assertThat(country.name()).isEqualTo(country.id()));
    }
}
