package com.k9x.application.dogs.port;

import com.k9x.application.dogs.use_case.dto.DogParticipationDTO;

import java.util.List;

public interface GetDogParticipationsPersistencePort {

    /**
     * Every event the dog was entered in whose event, stage and competition are all active, with its snapshot
     * results when the daily cron has already frozen them. Unordered: arranging them is the service case's job.
     */
    List<DogParticipationDTO> getParticipations(String dogIdentification);
}
