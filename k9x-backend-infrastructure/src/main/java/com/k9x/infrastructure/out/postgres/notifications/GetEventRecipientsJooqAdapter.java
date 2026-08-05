package com.k9x.infrastructure.out.postgres.notifications;

import com.k9x.application.notifications.port.GetEventRecipientsPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Dogs;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Events;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.UserSubscriptions;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves who to notify about a set of events: the owners of the dogs currently enrolled, plus the users who
 * explicitly subscribed to any of those events. Deriving the roster half on read instead of maintaining a
 * subscription table means a soft-deleted dog or a transferred ownership takes effect immediately, with
 * nothing to keep in sync; the subscribed half comes from {@code k9x.user_subscriptions}, where a user opts in
 * by hand. Somebody who is both an enrolled dog's owner and a subscriber is notified once (UNION).
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
        return new LinkedHashSet<>(
                competitorOwners(eventIds).union(subscribers(eventIds)).fetch(0, String.class));
    }

    private Select<Record1<String>> competitorOwners(List<String> eventIds) {
        EventCompetitors ec = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;
        Dogs d = com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.DOGS;
        Events e = com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.EVENTS;
        return dsl.selectDistinct(d.OWNER)
                .from(ec)
                .join(d).on(d.ID.eq(ec.DOG_ID))
                .join(e).on(e.ID.eq(ec.EVENT_ID))
                .where(ec.EVENT_ID.in(eventIds))
                .and(d.DELETED_AT.isNull())
                .and(d.OWNER.isNotNull())
                .and(e.DELETED_AT.isNull());
    }

    /**
     * Users whose subscription list overlaps the notified events. {@code &&} is the Postgres array overlap
     * operator, so one indexable predicate covers the whole event set.
     */
    private Select<Record1<String>> subscribers(List<String> eventIds) {
        UserSubscriptions us = com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.USER_SUBSCRIPTIONS;
        return dsl.selectDistinct(us.USER_ID)
                .from(us)
                .where(DSL.condition("{0} && {1}", us.EVENT_IDS,
                        DSL.val(eventIds.toArray(String[]::new), us.EVENT_IDS.getDataType())));
    }
}
