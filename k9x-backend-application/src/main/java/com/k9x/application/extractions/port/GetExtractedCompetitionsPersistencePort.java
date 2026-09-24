package com.k9x.application.extractions.port;

import com.k9x.application.extractions.use_case.dto.FetchExtractionLogStageDTO;

import java.util.List;

public interface GetExtractedCompetitionsPersistencePort {

    /**
     * Every active stage of an active competition loaded by an external ETL (source EXTRACTION) whose latest
     * extraction has a load instant, with its active events. Competitions created through the app are left out.
     */
    List<FetchExtractionLogStageDTO> getExtractedStages();
}
