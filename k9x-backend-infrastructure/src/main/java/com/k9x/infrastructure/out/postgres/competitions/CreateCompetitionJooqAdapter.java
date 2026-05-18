package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.port.CreateCompetitionPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class CreateCompetitionJooqAdapter implements CreateCompetitionPersistencePort {

    private final DSLContext dsl;

    public CreateCompetitionJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void createCompetition(String id, String name, String creator, long createdAt) {
        dsl.insertInto(Tables.COMPETITIONS)
                .set(Tables.COMPETITIONS.ID, id)
                .set(Tables.COMPETITIONS.NAME, name)
                .set(Tables.COMPETITIONS.COUNTRY, "")
                .set(Tables.COMPETITIONS.CREATOR, creator)
                .set(Tables.COMPETITIONS.CREATED_AT, createdAt)
                .set(Tables.COMPETITIONS.LAST_UPDATE, createdAt)
                .execute();
    }
}
