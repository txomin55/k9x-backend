package com.k9x.infrastructure.out.postgres.judges;

import com.k9x.application.judges.port.CreateJudgePersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class CreateJudgeJooqAdapter implements CreateJudgePersistencePort {

    private final DSLContext dsl;

    public CreateJudgeJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void createJudge(String id, String name, String country, String creator, long createdAt) {
        dsl.insertInto(Tables.JUDGES)
                .set(Tables.JUDGES.ID, id)
                .set(Tables.JUDGES.NAME, name)
                .set(Tables.JUDGES.COUNTRY, country)
                .set(Tables.JUDGES.CREATOR, creator)
                .set(Tables.JUDGES.CREATED_AT, createdAt)
                .set(Tables.JUDGES.LAST_UPDATE, createdAt)
                .execute();
    }
}
