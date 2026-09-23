package com.k9x.application.extractions.port;

import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;

import java.util.List;

public interface GetExtractedCompetitionsPersistencePort {

    /**
     * Hydrates every active competition loaded by an external ETL (source EXTRACTION), with its latest
     * extraction and its stages → events → competitors. Competitions created through the app are left out.
     */
    List<CompetitionSnapshot> getExtractedCompetitions();
}
