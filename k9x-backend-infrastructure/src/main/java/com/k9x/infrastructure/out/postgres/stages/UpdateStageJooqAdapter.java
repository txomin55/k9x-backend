package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.payload.UpdateStagePersistencePayload;
import com.k9x.application.stages.port.UpdateStagePersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class UpdateStageJooqAdapter implements UpdateStagePersistencePort {

    private final DSLContext dsl;

    public UpdateStageJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updateStage(String id, UpdateStagePersistencePayload payload) {
        dsl.update(Tables.STAGES)
                .set(Tables.STAGES.NAME, payload.name())
                .set(Tables.STAGES.DATE_FROM, payload.dateFrom())
                .set(Tables.STAGES.DATE_TO, payload.dateTo())
                .set(Tables.STAGES.LAST_UPDATE, payload.lastUpdate())
                .where(Tables.STAGES.ID.eq(id))
                .execute();
    }
}
