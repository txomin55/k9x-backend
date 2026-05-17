package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.port.UpdateStagePersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class UpdateStageJooqAdapter implements UpdateStagePersistencePort {

    private final DSLContext dsl;

    public UpdateStageJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updateStage(String id, String name, Long dateFrom, Long dateTo, long lastUpdate) {
        dsl.update(Tables.STAGES)
                .set(Tables.STAGES.NAME, name)
                .set(Tables.STAGES.DATE_FROM, dateFrom)
                .set(Tables.STAGES.DATE_TO, dateTo)
                .set(Tables.STAGES.LAST_UPDATE, lastUpdate)
                .where(Tables.STAGES.ID.eq(id))
                .execute();
    }
}
