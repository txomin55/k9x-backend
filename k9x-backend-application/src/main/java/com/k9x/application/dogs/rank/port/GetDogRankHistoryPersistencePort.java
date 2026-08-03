package com.k9x.application.dogs.rank.port;

import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankDTO;

import java.util.List;

public interface GetDogRankHistoryPersistencePort {

    /** Every {@code k9x.snap_dog_rank} row for the given discipline (the full history of every dog). */
    List<FetchDogRankDTO> getDogRankHistory(String discipline);
}
