package com.k9x.configuration.secured.breeds;

import com.k9x.application.breeds.port.GetBreedListPort;
import com.k9x.application.breeds.use_case.GetBreedListServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BreedUseCaseConfiguration {

    @Bean
    public GetBreedListServiceCase getBreedListServiceCase(GetBreedListPort getBreedListPort) {
        return new GetBreedListServiceCase(getBreedListPort);
    }
}
