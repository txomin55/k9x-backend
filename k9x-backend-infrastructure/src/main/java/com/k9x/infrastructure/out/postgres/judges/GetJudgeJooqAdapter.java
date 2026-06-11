package com.k9x.infrastructure.out.postgres.judges;

import com.k9x.application.judges.port.GetJudgePersistencePort;
import com.k9x.domain.judges.aggregates.Judge;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

public class GetJudgeJooqAdapter implements GetJudgePersistencePort {

    private final DSLContext dsl;

    public GetJudgeJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Judge getJudge(String id) {
        return dsl.select()
                .from(Tables.JUDGES)
                .where(Tables.JUDGES.ID.eq(id))
                .fetchOptional(r -> new Judge(
                        r.get(Tables.JUDGES.ID),
                        r.get(Tables.JUDGES.NAME),
                        r.get(Tables.JUDGES.CREATOR),
                        r.get(Tables.JUDGES.LAST_UPDATE),
                        r.get(Tables.JUDGES.CREATED_AT),
                        r.get(Tables.JUDGES.DELETED_AT)
                )).orElse(null);
    }
}
