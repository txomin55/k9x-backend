package com.k9x.application.dogs.rank.port;

import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankingEntryDTO;

import java.util.Optional;

public interface GetDogRankingEntryPersistencePort {

    /** The dog's row of the ranking snapshot, empty when it is not in it or is no longer active. */
    Optional<FetchDogRankingEntryDTO> getEntry(String dogIdentification);
}
