package com.k9x.configuration.dogrank;

import com.k9x.application.dogs.rank.port.CreateDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogRankEventResultsPersistencePort;
import com.k9x.application.dogs.rank.port.GetLatestDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.use_case.GenerateDogRankHistoryServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DogRankUseCaseConfiguration {

    @Bean
    public GenerateDogRankHistoryServiceCase generateDogRankHistoryServiceCase(
            GetDogRankEventResultsPersistencePort getDogRankEventResultsPersistencePort,
            GetLatestDogRankHistoryPersistencePort getLatestDogRankHistoryPersistencePort,
            CreateDogRankHistoryPersistencePort createDogRankHistoryPersistencePort) {
        return new GenerateDogRankHistoryServiceCase(
                getDogRankEventResultsPersistencePort,
                getLatestDogRankHistoryPersistencePort,
                createDogRankHistoryPersistencePort);
    }
}
