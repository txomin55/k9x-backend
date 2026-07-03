package com.k9x.configuration.secured.countries;

import com.k9x.application.countries.port.GetCountryListPort;
import com.k9x.application.countries.use_case.GetCountryListServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CountryUseCaseConfiguration {

    @Bean
    public GetCountryListServiceCase getCountryListServiceCase(GetCountryListPort getCountryListPort) {
        return new GetCountryListServiceCase(getCountryListPort);
    }
}
