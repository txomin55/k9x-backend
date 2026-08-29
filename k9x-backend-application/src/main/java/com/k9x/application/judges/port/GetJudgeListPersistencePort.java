package com.k9x.application.judges.port;

import com.k9x.domain.judges.aggregates.Judge;

import java.util.List;

public interface GetJudgeListPersistencePort {

    /** Both filters are optional: a {@code null} leaves that side of the list unfiltered. */
    List<Judge> getJudges(String creator, String country);
}
