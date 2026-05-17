package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class GetStageJooqAdapter implements GetStagePersistencePort {

    private final DSLContext dsl;

    public GetStageJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Stage getStage(String id) {
        return dsl.select()
                .from(Tables.STAGES)
                .where(Tables.STAGES.ID.eq(id))
                .fetchOptional(r -> new Stage(
                        r.get(Tables.STAGES.ID),
                        r.get(Tables.STAGES.NAME),
                        r.get(Tables.STAGES.COMPETITION_ID),
                        r.get(Tables.STAGES.CREATOR),
                        r.get(Tables.STAGES.LAST_UPDATE),
                        r.get(Tables.STAGES.CREATED_AT),
                        r.get(Tables.STAGES.DELETED_AT)
                )).orElse(null);
    }
}
