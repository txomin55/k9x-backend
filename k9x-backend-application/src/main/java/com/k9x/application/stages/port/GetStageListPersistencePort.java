package com.k9x.application.stages.port;

import com.k9x.application.stages.use_case.dto.FetchStageListRowDTO;

import java.util.List;

public interface GetStageListPersistencePort {

    /**
     * The active stages of active competitions whose {@code dateFrom} falls within [{@code from}, {@code to}]
     * (a {@code null} bound is open-ended) and, when {@code country} is given, whose competition is of that
     * country, filtered in the database. {@code allCompetitorsSettled} is only
     * resolved for events of stages whose {@code dateTo} is on or after {@code startOfTodayUtcMillis}: an older
     * stage is FINISHED by date and the flag would never be read.
     */
    List<FetchStageListRowDTO> getStages(Long from, Long to, String country, long startOfTodayUtcMillis);
}
