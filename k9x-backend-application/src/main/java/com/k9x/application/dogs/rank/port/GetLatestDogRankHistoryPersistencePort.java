package com.k9x.application.dogs.rank.port;

import com.k9x.application.dogs.rank.use_case.dto.FetchLatestDogRankHistoryDTO;

import java.util.List;

public interface GetLatestDogRankHistoryPersistencePort {

    /** The most recent {@code k9x.snap_dog_index_history} record of each of the given dogs that has one. */
    List<FetchLatestDogRankHistoryDTO> getLatestHistory(List<String> dogIdentifications);
}
