package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.port.DeleteStagePersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class DeleteStageJooqAdapter implements DeleteStagePersistencePort {

    private final DSLContext dsl;

    public DeleteStageJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void deleteStage(String id, long deletedAt) {
        dsl.update(Tables.STAGES)
                .set(Tables.STAGES.DELETED_AT, deletedAt)
                .where(Tables.STAGES.ID.eq(id))
                .execute();
    }
}
