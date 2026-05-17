package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.domain.aggregates.competitions.Competition;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class GetCompetitionJooqAdapter implements GetCompetitionPersistencePort {

    private final DSLContext dsl;

    public GetCompetitionJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Competition getCompetition(String id) {
        return dsl.select()
                .from(Tables.COMPETITIONS)
                .where(Tables.COMPETITIONS.ID.eq(id))
                .fetchOptional(r -> new Competition(
                        r.get(Tables.COMPETITIONS.ID),
                        r.get(Tables.COMPETITIONS.NAME),
                        r.get(Tables.COMPETITIONS.CREATOR),
                        r.get(Tables.COMPETITIONS.LAST_UPDATE),
                        r.get(Tables.COMPETITIONS.CREATED_AT),
                        r.get(Tables.COMPETITIONS.DELETED_AT)
                )).orElse(null);
    }
}
