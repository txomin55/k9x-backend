package com.k9x.infrastructure.out.postgres.judges;

import com.k9x.application.judges.port.DeleteJudgePersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class DeleteJudgeJooqAdapter implements DeleteJudgePersistencePort {

    private final DSLContext dsl;

    public DeleteJudgeJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void deleteJudge(String id, long deletedAt) {
        dsl.update(Tables.JUDGES)
                .set(Tables.JUDGES.DELETED_AT, deletedAt)
                .where(Tables.JUDGES.ID.eq(id))
                .execute();
    }
}
