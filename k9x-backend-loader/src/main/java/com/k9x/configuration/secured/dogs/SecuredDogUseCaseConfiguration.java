package com.k9x.configuration.secured.dogs;

import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.application.dogs.use_case.GetDogListServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredDogUseCaseConfiguration {

    @Bean
    public GetDogListServiceCase getDogListServiceCase(GetDogListPersistencePort getDogListPersistencePort) {
        return new GetDogListServiceCase(getDogListPersistencePort);
    }
}
