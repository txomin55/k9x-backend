package com.k9x.infrastructure.out.postgres.judges;

import com.k9x.application.judges.port.GetJudgeListPersistencePort;
import com.k9x.domain.judges.aggregates.Judge;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.util.List;

public class GetJudgeListJooqAdapter implements GetJudgeListPersistencePort {

    private final DSLContext dsl;

    public GetJudgeListJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Judge> getJudges(String creator) {
        return dsl.select()
                .from(Tables.JUDGES)
                .where(Tables.JUDGES.CREATOR.eq(creator))
                .and(Tables.JUDGES.DELETED_AT.isNull())
                .fetch(r -> new Judge(
                        r.get(Tables.JUDGES.ID),
                        r.get(Tables.JUDGES.NAME),
                        r.get(Tables.JUDGES.CREATOR),
                        r.get(Tables.JUDGES.COUNTRY),
                        r.get(Tables.JUDGES.LAST_UPDATE),
                        r.get(Tables.JUDGES.CREATED_AT),
                        r.get(Tables.JUDGES.DELETED_AT)
                ));
    }
}
