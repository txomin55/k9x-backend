package com.k9x.configuration.secured.dogs;

import com.k9x.application.dogs.port.CreateDogPersistencePort;
import com.k9x.application.dogs.port.DeleteDogPersistencePort;
import com.k9x.application.dogs.port.GetDogListPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.port.UpdateDogPersistencePort;
import com.k9x.application.dogs.use_case.CreateDogServiceCase;
import com.k9x.application.dogs.use_case.DeleteDogServiceCase;
import com.k9x.application.dogs.use_case.GetDogListServiceCase;
import com.k9x.application.dogs.use_case.UpdateDogServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecuredDogUseCaseConfiguration {

    @Bean
    public CreateDogServiceCase createDogServiceCase(CreateDogPersistencePort createDogPersistencePort,
                                                      GetDogPersistencePort getDogPersistencePort) {
        return new CreateDogServiceCase(createDogPersistencePort, getDogPersistencePort);
    }

    @Bean
    public GetDogListServiceCase getDogListServiceCase(GetDogListPersistencePort getDogListPersistencePort) {
        return new GetDogListServiceCase(getDogListPersistencePort);
    }

    @Bean
    public DeleteDogServiceCase deleteDogServiceCase(GetDogPersistencePort getDogPersistencePort,
                                                     DeleteDogPersistencePort deleteDogPersistencePort) {
        return new DeleteDogServiceCase(getDogPersistencePort, deleteDogPersistencePort);
    }

    @Bean
    public UpdateDogServiceCase updateDogServiceCase(GetDogPersistencePort getDogPersistencePort,
                                                     UpdateDogPersistencePort updateDogPersistencePort) {
        return new UpdateDogServiceCase(getDogPersistencePort, updateDogPersistencePort);
    }
}
