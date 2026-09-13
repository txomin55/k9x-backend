package com.k9x.configuration.dogs;

import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.port.GetPublicDogListPersistencePort;
import com.k9x.application.dogs.use_case.GetPublicDogListServiceCase;
import com.k9x.application.dogs.use_case.GetPublicDogServiceCase;
import com.k9x.infrastructure.in.rest.endpoints.dogs.FetchAllDogs;
import com.k9x.infrastructure.in.rest.endpoints.dogs.FetchDog;
import com.k9x.infrastructure.in.rest.i18n.ReferenceNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring for the public dog directory. Lives outside {@code configuration.secured} and takes no
 * {@code UserInfoDTO}, matching the other public endpoints.
 */
@Configuration
public class DogsEndpointConfiguration {

    @Bean
    public GetPublicDogListServiceCase getPublicDogListServiceCase(
            GetPublicDogListPersistencePort getPublicDogListPersistencePort) {
        return new GetPublicDogListServiceCase(getPublicDogListPersistencePort);
    }

    @Bean
    public GetPublicDogServiceCase getPublicDogServiceCase(GetDogPersistencePort getDogPersistencePort) {
        return new GetPublicDogServiceCase(getDogPersistencePort);
    }

    @Bean
    public FetchAllDogs fetchAllDogs(GetPublicDogListServiceCase getPublicDogListServiceCase,
                                     ReferenceNameResolver referenceNameResolver) {
        return new FetchAllDogs(getPublicDogListServiceCase, referenceNameResolver);
    }

    @Bean
    public FetchDog fetchDog(GetPublicDogServiceCase getPublicDogServiceCase,
                             ReferenceNameResolver referenceNameResolver) {
        return new FetchDog(getPublicDogServiceCase, referenceNameResolver);
    }
}
