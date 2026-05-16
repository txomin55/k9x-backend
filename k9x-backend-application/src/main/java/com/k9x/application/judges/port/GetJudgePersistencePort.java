package com.k9x.application.judges.port;

import com.k9x.domain.aggregates.judges.Judge;

public interface GetJudgePersistencePort {

    Judge getJudge(String id);
}
