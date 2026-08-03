package com.k9x.application.dogs.rank.port;

import com.k9x.application.dogs.rank.port.payload.DogRankHistoryPayload;

import java.util.List;

public interface CreateDogRankHistoryPersistencePort {

    void create(List<DogRankHistoryPayload> records);
}
