package com.k9x.configuration.secured.countries;

import com.k9x.application.countries.use_case.GetCountryListServiceCase;
import com.k9x.infrastructure.in.rest.endpoints.secured.countries.FetchCountries;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredCountriesEndpointConfiguration {

    @Bean
    public FetchCountries fetchCountries(GetCountryListServiceCase getCountryListServiceCase) {
        return new FetchCountries(getCountryListServiceCase);
    }
}
