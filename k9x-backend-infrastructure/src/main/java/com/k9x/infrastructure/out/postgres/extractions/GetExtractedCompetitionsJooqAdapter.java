package com.k9x.infrastructure.out.postgres.extractions;

import com.k9x.application.extractions.port.GetExtractedCompetitionsPersistencePort;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.competitions.aggregates.CompetitionSource;
import com.k9x.infrastructure.out.postgres.competitions.CompetitionHydrator;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.util.List;

public class GetExtractedCompetitionsJooqAdapter implements GetExtractedCompetitionsPersistencePort {

    private final CompetitionHydrator hydrator;

    public GetExtractedCompetitionsJooqAdapter(DSLContext dsl) {
        this.hydrator = new CompetitionHydrator(dsl);
    }

    @Override
    public List<CompetitionSnapshot> getExtractedCompetitions() {
        return hydrator.hydrate(Tables.COMPETITIONS.SOURCE.eq(CompetitionSource.EXTRACTION.name())
                .and(Tables.COMPETITIONS.DELETED_AT.isNull()));
    }
}
