package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.port.CreateStagePersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class CreateStageJooqAdapter implements CreateStagePersistencePort {

    private final DSLContext dsl;

    public CreateStageJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void createStage(String id, String name, String competitionId, Long dateFrom, Long dateTo,
                            String creator, long createdAt) {
        dsl.insertInto(Tables.STAGES)
                .set(Tables.STAGES.ID, id)
                .set(Tables.STAGES.NAME, name)
                .set(Tables.STAGES.COMPETITION_ID, competitionId)
                .set(Tables.STAGES.DATE_FROM, dateFrom)
                .set(Tables.STAGES.DATE_TO, dateTo)
                .set(Tables.STAGES.CREATOR, creator)
                .set(Tables.STAGES.CREATED_AT, createdAt)
                .set(Tables.STAGES.LAST_UPDATE, createdAt)
                .execute();
    }
}
