package com.k9x.infrastructure.out.postgres.events;

import com.k9x.application.events.port.GetEventClassificationHeaderPersistencePort;
import com.k9x.application.events.use_case.dto.FetchEventClassificationHeaderDTO;
import com.k9x.infrastructure.out.postgres.competitions.LatestExtractionQuery;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Competitions;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Events;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Stages;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventInfo;
import org.jooq.DSLContext;

import java.util.Optional;

/**
 * Query projection of a classification's header: one row joining the event to its stage and competition, with
 * whether the event holds any score aggregated in SQL, plus the competition's latest extraction. No competitor,
 * score or sibling row is loaded.
 */
public class GetEventClassificationHeaderJooqAdapter implements GetEventClassificationHeaderPersistencePort {

    private static final Events EV = Tables.EVENTS;
    private static final Stages ST = Tables.STAGES;
    private static final Competitions CO = Tables.COMPETITIONS;
    private static final EventInfo EI = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_INFO;

    private final DSLContext dsl;

    public GetEventClassificationHeaderJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Optional<FetchEventClassificationHeaderDTO> getHeader(String eventId) {
        return dsl.select(EV.ID, EV.NAME, EV.DISCIPLINE, EI.CONFIGURATION_ID, EV.DELETED_AT, EV.RANK_SCORE,
                        EventProjectionFields.HAS_ANY_SCORE, ST.ID, ST.NAME, ST.DATE_TO, CO.ID, CO.NAME)
                .from(EV)
                .join(ST).on(ST.ID.eq(EV.STAGE_ID))
                .join(CO).on(CO.ID.eq(ST.COMPETITION_ID))
                // LEFT JOIN on purpose: the OBDX settings row is written by the event update, so a just-created
                // event — or one of another discipline — legitimately has none.
                .leftJoin(EI).on(EI.EVENT_ID.eq(EV.ID))
                .where(EV.ID.eq(eventId))
                .fetchOptional(r -> new FetchEventClassificationHeaderDTO(r.get(EV.ID), r.get(EV.NAME),
                        r.get(EV.DISCIPLINE), r.get(EI.CONFIGURATION_ID), r.get(EV.DELETED_AT), r.get(EV.RANK_SCORE),
                        Boolean.TRUE.equals(r.get(EventProjectionFields.HAS_ANY_SCORE)), r.get(ST.ID), r.get(ST.NAME),
                        r.get(ST.DATE_TO), r.get(CO.NAME), LatestExtractionQuery.of(dsl, r.get(CO.ID))));
    }
}
