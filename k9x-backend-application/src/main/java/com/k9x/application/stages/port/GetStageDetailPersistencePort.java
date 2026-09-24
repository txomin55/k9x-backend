package com.k9x.application.stages.port;

import com.k9x.application.stages.use_case.dto.FetchStageDetailRowDTO;

import java.util.Optional;

public interface GetStageDetailPersistencePort {

    /**
     * The stage with the given id, soft-deleted or not, with its events and their competitors; empty when no
     * such stage exists. {@code allCompetitorsSettled} is only resolved when the stage's {@code dateTo} is on or
     * after {@code startOfTodayUtcMillis}: an older stage is FINISHED by date and the flag would never be read.
     */
    Optional<FetchStageDetailRowDTO> getStage(String stageId, long startOfTodayUtcMillis);
}
