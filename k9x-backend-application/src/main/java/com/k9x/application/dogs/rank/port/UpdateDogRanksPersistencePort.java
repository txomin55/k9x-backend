package com.k9x.application.dogs.rank.port;

import com.k9x.application.dogs.rank.port.payload.DogRankUpdatePayload;

import java.util.List;

public interface UpdateDogRanksPersistencePort {

    void updateRanks(List<DogRankUpdatePayload> ranks);
}
