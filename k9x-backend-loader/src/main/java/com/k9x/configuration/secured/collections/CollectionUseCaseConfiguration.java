package com.k9x.configuration.secured.collections;

import com.k9x.application.collections.port.GetCollectionListPersistencePort;
import com.k9x.application.collections.use_case.GetCollectionListServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CollectionUseCaseConfiguration {

    @Bean
    public GetCollectionListServiceCase getCollectionListServiceCase(GetCollectionListPersistencePort getCollectionListPersistencePort) {
        return new GetCollectionListServiceCase(getCollectionListPersistencePort);
    }
}
