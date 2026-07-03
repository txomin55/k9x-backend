package com.k9x.infrastructure.out.enums.countries;

import com.k9x.application.countries.port.GetCountryListPort;
import com.k9x.application.countries.use_case.dto.CountryDTO;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Arrays;
import java.util.List;

public class CountryEnumAdapter implements GetCountryListPort {

    private final MessageSource messageSource;

    public CountryEnumAdapter(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public List<CountryDTO> getCountries() {
        return Arrays.stream(Country.values())
                .map(country -> new CountryDTO(country.name(), translate(country)))
                .toList();
    }

    private String translate(Country country) {
        String key = "country." + country.name().toLowerCase() + ".name";
        return messageSource.getMessage(key, null, country.name(), LocaleContextHolder.getLocale());
    }
}
