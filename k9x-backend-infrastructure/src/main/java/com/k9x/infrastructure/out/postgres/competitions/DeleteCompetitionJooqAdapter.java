package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.port.DeleteCompetitionPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class DeleteCompetitionJooqAdapter implements DeleteCompetitionPersistencePort {

    private final DSLContext dsl;

    public DeleteCompetitionJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void deleteCompetition(String id, long deletedAt) {
        dsl.update(Tables.COMPETITIONS)
                .set(Tables.COMPETITIONS.DELETED_AT, deletedAt)
                .where(Tables.COMPETITIONS.ID.eq(id))
                .execute();
    }
}
