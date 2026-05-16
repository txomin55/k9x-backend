package com.k9x.infrastructure.out.postgres.judges;

import com.k9x.application.judges.port.UpdateJudgePersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class UpdateJudgeJooqAdapter implements UpdateJudgePersistencePort {

    private final DSLContext dsl;

    public UpdateJudgeJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updateJudge(String id, String name, long lastUpdate) {
        dsl.update(Tables.JUDGES)
                .set(Tables.JUDGES.NAME, name)
                .set(Tables.JUDGES.LAST_UPDATE, lastUpdate)
                .where(Tables.JUDGES.ID.eq(id))
                .execute();
    }
}
