package com.k9x.application.dogs.rank.port.payload;

import com.k9x.application.utils.date.DateUtils;

public record DogRankUpdatePayload(String dogId, int rank, long lastUpdate) {

    public static DogRankUpdatePayload from(String dogId, int rank) {
        return new DogRankUpdatePayload(dogId, rank, DateUtils.nowUtcMillis());
    }
}
