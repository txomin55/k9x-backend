package com.k9x.application.dogs.rank.port;

import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankEventResultDTO;

import java.util.List;

public interface GetDogRankEventResultsPersistencePort {

    /**
     * The {@code k9x.snap_dog_rank} rows of the given dogs across all disciplines (their raw per-event rank
     * scores, as written by the snapshot cron), oldest first per dog.
     */
    List<FetchDogRankEventResultDTO> getEventResults(List<String> dogIdentifications);
}
