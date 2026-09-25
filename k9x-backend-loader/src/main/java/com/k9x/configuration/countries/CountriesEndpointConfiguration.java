package com.k9x.configuration.countries;

import com.k9x.application.countries.use_case.GetCountryListServiceCase;
import com.k9x.infrastructure.in.rest.endpoints.countries.FetchPublicCountries;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CountriesEndpointConfiguration {

    @Bean
    public FetchPublicCountries fetchPublicCountries(GetCountryListServiceCase getCountryListServiceCase) {
        return new FetchPublicCountries(getCountryListServiceCase);
    }
}
