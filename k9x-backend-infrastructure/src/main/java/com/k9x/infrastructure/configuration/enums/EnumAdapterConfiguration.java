package com.k9x.infrastructure.configuration.enums;

import com.k9x.application.awards.port.GetAwardListPort;
import com.k9x.application.breeds.port.GetBreedListPort;
import com.k9x.application.countries.port.GetCountryListPort;
import com.k9x.infrastructure.out.enums.awards.AwardEnumAdapter;
import com.k9x.infrastructure.out.enums.breeds.BreedEnumAdapter;
import com.k9x.infrastructure.out.enums.countries.CountryEnumAdapter;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnumAdapterConfiguration {

    @Bean
    public GetCountryListPort getCountryListPort(MessageSource messageSource) {
        return new CountryEnumAdapter(messageSource);
    }

    @Bean
    public GetBreedListPort getBreedListPort(MessageSource messageSource) {
        return new BreedEnumAdapter(messageSource);
    }

    @Bean
    public GetAwardListPort getAwardListPort() {
        return new AwardEnumAdapter();
    }
}
