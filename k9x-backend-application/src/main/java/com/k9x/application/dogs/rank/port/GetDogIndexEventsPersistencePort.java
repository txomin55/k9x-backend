package com.k9x.application.dogs.rank.port;

import com.k9x.application.dogs.rank.use_case.dto.FetchDogIndexEventDTO;

import java.util.List;

public interface GetDogIndexEventsPersistencePort {

    /**
     * Every snapshotted result of the dog that feeds its index ({@code k9x.snap_dog_rank}), with the event,
     * competition and final placing it came from. Unordered: arranging them is the service case's job.
     */
    List<FetchDogIndexEventDTO> getIndexEvents(String dogIdentification);
}
