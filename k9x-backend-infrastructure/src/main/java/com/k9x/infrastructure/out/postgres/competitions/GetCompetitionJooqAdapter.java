package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.port.GetCompetitionPersistencePort;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.util.List;

public class GetCompetitionJooqAdapter implements GetCompetitionPersistencePort {

    private final DSLContext dsl;
    private final CompetitionHydrator hydrator;

    public GetCompetitionJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
        this.hydrator = new CompetitionHydrator(dsl);
    }

    @Override
    public CompetitionSnapshot getCompetition(String id) {
        List<CompetitionSnapshot> competitions = hydrator.hydrate(Tables.COMPETITIONS.ID.eq(id));
        return competitions.isEmpty() ? null : competitions.getFirst();
    }

    @Override
    public String competitionIdByStage(String stageId) {
        return dsl.select(Tables.STAGES.COMPETITION_ID)
                .from(Tables.STAGES)
                .where(Tables.STAGES.ID.eq(stageId))
                .fetchOptional(r -> r.get(Tables.STAGES.COMPETITION_ID))
                .orElse(null);
    }

    @Override
    public String competitionIdByEvent(String eventId) {
        return dsl.select(Tables.STAGES.COMPETITION_ID)
                .from(Tables.EVENTS)
                .join(Tables.STAGES).on(Tables.STAGES.ID.eq(Tables.EVENTS.STAGE_ID))
                .where(Tables.EVENTS.ID.eq(eventId))
                .fetchOptional(r -> r.get(Tables.STAGES.COMPETITION_ID))
                .orElse(null);
    }
}
