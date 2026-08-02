package com.k9x.configuration.dogrank;

import com.k9x.application.dogs.rank.port.GetDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.UpdateDogRanksPersistencePort;
import com.k9x.application.dogs.rank.use_case.UpdateDogRanksServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DogRankUseCaseConfiguration {

    @Bean
    public UpdateDogRanksServiceCase updateDogRanksServiceCase(
            GetDogRankHistoryPersistencePort getDogRankHistoryPersistencePort,
            UpdateDogRanksPersistencePort updateDogRanksPersistencePort) {
        return new UpdateDogRanksServiceCase(getDogRankHistoryPersistencePort, updateDogRanksPersistencePort);
    }
}
