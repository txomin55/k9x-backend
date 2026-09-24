package com.k9x.infrastructure.out.postgres.events;

import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Events;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventScores;
import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 * Per-event facts that query projections aggregate in SQL instead of loading the child rows: correlated to
 * {@code k9x.events}, so they are meant for a select over that table.
 */
public final class EventProjectionFields {

    private static final Events EV = Tables.EVENTS;
    private static final EventCompetitors EC =
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;
    private static final EventScores ES = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_SCORES;

    /** How many competitors are enrolled in the event. */
    public static final Field<Integer> COMPETITOR_COUNT =
            DSL.selectCount().from(EC).where(EC.EVENT_ID.eq(EV.ID)).asField("competitor_count");

    /** Whether the event holds at least one recorded score. */
    public static final Field<Boolean> HAS_ANY_SCORE =
            DSL.field(DSL.exists(DSL.selectOne().from(ES).where(ES.EVENT_ID.eq(EV.ID)).and(ES.SCORE.isNotNull())))
                    .as("has_any_score");

    private EventProjectionFields() {
    }
}
