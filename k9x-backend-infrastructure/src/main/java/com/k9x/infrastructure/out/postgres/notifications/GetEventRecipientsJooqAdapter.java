package com.k9x.infrastructure.out.postgres.notifications;

import com.k9x.application.notifications.port.GetEventRecipientsPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Dogs;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Events;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import org.jooq.DSLContext;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves who to notify about a set of events straight from the rosters: the owners of the dogs currently
 * enrolled. Deriving this on read instead of maintaining a subscription table means a soft-deleted dog or a
 * transferred ownership takes effect immediately, with nothing to keep in sync.
 *
 * <p>Dogs without an owner are skipped (there is nobody to address), as are soft-deleted dogs and events.
 * Competitors flagged {@code not_competing} or {@code reserve} are deliberately kept: they are still part of
 * the event and still want to hear about it.
 */
public class GetEventRecipientsJooqAdapter implements GetEventRecipientsPersistencePort {

    private final DSLContext dsl;

    public GetEventRecipientsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Set<String> getRecipientIds(List<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Set.of();
        }
        EventCompetitors ec = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;
        Dogs d = com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.DOGS;
        Events e = com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.EVENTS;
        return new LinkedHashSet<>(dsl.selectDistinct(d.OWNER)
                .from(ec)
                .join(d).on(d.ID.eq(ec.DOG_ID))
                .join(e).on(e.ID.eq(ec.EVENT_ID))
                .where(ec.EVENT_ID.in(eventIds))
                .and(d.DELETED_AT.isNull())
                .and(d.OWNER.isNotNull())
                .and(e.DELETED_AT.isNull())
                .fetch(d.OWNER));
    }
}
