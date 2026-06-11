package com.k9x.application.judges.port;

import com.k9x.domain.judges.aggregates.Judge;

import java.util.List;

public interface GetJudgeListPersistencePort {

    List<Judge> getJudges(String creator);
}
