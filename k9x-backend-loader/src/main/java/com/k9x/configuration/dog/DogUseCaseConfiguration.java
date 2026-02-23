package com.k9x.configuration.dog;

import com.k9x.application.dog.action.GetDogListServiceCase;
import com.k9x.application.dog.action.GetDogServiceCase;
import com.k9x.application.dog.port.GetDogListPersistencePort;
import com.k9x.application.dog.port.GetDogPersistencePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DogUseCaseConfiguration {

    @Bean
    public GetDogServiceCase getDogServiceCase(GetDogPersistencePort getDogPersistencePort) {
        return new GetDogServiceCase(getDogPersistencePort);
    }

    @Bean
    public GetDogListServiceCase getDogListServiceCase(GetDogListPersistencePort getDogListPersistencePort) {
        return new GetDogListServiceCase(getDogListPersistencePort);
    }
}
