package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.port.GetCompetitionListPersistencePort;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.util.List;

public class GetCompetitionListJooqAdapter implements GetCompetitionListPersistencePort {

    private final CompetitionHydrator hydrator;

    public GetCompetitionListJooqAdapter(DSLContext dsl) {
        this.hydrator = new CompetitionHydrator(dsl);
    }

    @Override
    public List<CompetitionSnapshot> getCompetitions(String creator) {
        return hydrator.hydrate(Tables.COMPETITIONS.CREATOR.eq(creator)
                .and(Tables.COMPETITIONS.DELETED_AT.isNull()));
    }
}
