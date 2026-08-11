package com.k9x.infrastructure.out.postgres.rankings;

import com.k9x.application.rankings.port.GetRankedEventIdsPersistencePort;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;

import java.util.Set;

/**
 * One query for the whole stage list: the event ids of every ranking, flattened. Unfiltered on purpose — the
 * rankings table is small and this keeps it to a single round trip regardless of how many stages are being
 * listed, the same trade-off the announcements query makes.
 */
public class GetRankedEventIdsJooqAdapter implements GetRankedEventIdsPersistencePort {

    private static final Field<String> EVENT_ID =
            org.jooq.impl.DSL.field("event_id", SQLDataType.VARCHAR);

    private final DSLContext dsl;

    public GetRankedEventIdsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Set<String> getRankedEventIds() {
        return dsl.resultQuery(
                        "select distinct unnest(event_ids) as event_id from k9x.rankings")
                .fetchSet(EVENT_ID);
    }
}
