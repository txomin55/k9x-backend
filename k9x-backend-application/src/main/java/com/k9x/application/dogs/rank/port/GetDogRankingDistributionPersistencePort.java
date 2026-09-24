package com.k9x.application.dogs.rank.port;

import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankingDistributionDTO;

public interface GetDogRankingDistributionPersistencePort {

    /**
     * How many dogs of the ranking snapshot hold each index at or above {@code minIndex}, within {@code country}
     * when it is not null.
     */
    FetchDogRankingDistributionDTO getDistribution(String country, int minIndex);
}
