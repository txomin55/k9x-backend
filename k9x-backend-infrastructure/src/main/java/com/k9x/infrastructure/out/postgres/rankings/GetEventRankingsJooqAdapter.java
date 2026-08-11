package com.k9x.infrastructure.out.postgres.rankings;

import com.k9x.application.rankings.port.GetEventRankingsPersistencePort;
import com.k9x.application.shared.IdNameDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

public class GetEventRankingsJooqAdapter implements GetEventRankingsPersistencePort {

    private final DSLContext dsl;

    public GetEventRankingsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<IdNameDTO> getEventRankings(String eventId) {
        // The events live in an array column, so membership is an array containment check rather than a join.
        return dsl.select(Tables.RANKINGS.ID, Tables.RANKINGS.NAME)
                .from(Tables.RANKINGS)
                .where(DSL.condition("{0} = any({1})", DSL.val(eventId), Tables.RANKINGS.EVENT_IDS))
                .orderBy(Tables.RANKINGS.NAME.asc())
                .fetch(record -> new IdNameDTO(
                        record.get(Tables.RANKINGS.ID), record.get(Tables.RANKINGS.NAME)));
    }
}
