package com.k9x.configuration.dogrank;

import com.k9x.application.dogs.rank.port.CreateDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogRankDogIdentificationsPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogRankEventResultsPersistencePort;
import com.k9x.application.dogs.rank.port.GetLatestDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.ReplaceDogRankingSnapshotPersistencePort;
import com.k9x.application.dogs.rank.use_case.GenerateDogRankHistoryServiceCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DogRankUseCaseConfiguration {

    @Bean
    public GenerateDogRankHistoryServiceCase generateDogRankHistoryServiceCase(
            GetDogRankDogIdentificationsPersistencePort getDogRankDogIdentificationsPersistencePort,
            GetDogRankEventResultsPersistencePort getDogRankEventResultsPersistencePort,
            GetLatestDogRankHistoryPersistencePort getLatestDogRankHistoryPersistencePort,
            CreateDogRankHistoryPersistencePort createDogRankHistoryPersistencePort,
            ReplaceDogRankingSnapshotPersistencePort replaceDogRankingSnapshotPersistencePort,
            @Value("${k9x-backend.crons.dog-rank.block-size:200}") int blockSize) {
        return new GenerateDogRankHistoryServiceCase(
                getDogRankDogIdentificationsPersistencePort,
                getDogRankEventResultsPersistencePort,
                getLatestDogRankHistoryPersistencePort,
                createDogRankHistoryPersistencePort,
                replaceDogRankingSnapshotPersistencePort,
                blockSize);
    }
}
