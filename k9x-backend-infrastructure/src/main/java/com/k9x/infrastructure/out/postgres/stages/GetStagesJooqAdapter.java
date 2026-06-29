package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.infrastructure.out.postgres.competitions.CompetitionHydrator;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

public class GetStagesJooqAdapter implements GetStageListPersistencePort {

    private final CompetitionHydrator hydrator;

    public GetStagesJooqAdapter(DSLContext dsl) {
        this.hydrator = new CompetitionHydrator(dsl);
    }

    @Override
    public List<CompetitionSnapshot> getCompetitions() {
        return hydrator.hydrate(DSL.trueCondition());
    }
}
