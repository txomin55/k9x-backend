package com.k9x.application.rankings.port;

import com.k9x.application.rankings.port.payload.SaveRankingPersistencePayload;

public interface SaveRankingPersistencePort {

    void saveRanking(SaveRankingPersistencePayload payload);
}
