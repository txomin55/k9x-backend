package com.k9x.configuration.secured.breeds;

import com.k9x.application.breeds.use_case.GetBreedListServiceCase;
import com.k9x.infrastructure.in.rest.endpoints.secured.breeds.FetchBreeds;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredBreedsEndpointConfiguration {

    @Bean
    public FetchBreeds fetchBreeds(GetBreedListServiceCase getBreedListServiceCase) {
        return new FetchBreeds(getBreedListServiceCase);
    }
}
