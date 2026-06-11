package com.k9x.application.judges.port;

import com.k9x.domain.judges.aggregates.Judge;

public interface GetJudgePersistencePort {

    Judge getJudge(String id);
}
