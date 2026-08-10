package com.k9x.infrastructure.out.postgres.rankings;

import com.k9x.application.rankings.port.GetActiveEventIdsPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.util.Collection;
import java.util.Set;

public class GetActiveEventIdsJooqAdapter implements GetActiveEventIdsPersistencePort {

    private final DSLContext dsl;

    public GetActiveEventIdsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Set<String> getActiveEventIds(Collection<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Set.of();
        }
        return dsl.select(Tables.EVENTS.ID)
                .from(Tables.EVENTS)
                .where(Tables.EVENTS.ID.in(eventIds))
                .and(Tables.EVENTS.DELETED_AT.isNull())
                .fetchSet(Tables.EVENTS.ID);
    }
}
