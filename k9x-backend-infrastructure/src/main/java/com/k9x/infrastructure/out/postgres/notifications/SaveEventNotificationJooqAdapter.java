package com.k9x.infrastructure.out.postgres.notifications;

import com.k9x.application.notifications.port.SaveEventNotificationPersistencePort;
import com.k9x.application.notifications.port.payload.SaveEventNotificationPersistencePayload;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

/**
 * Stores an organizer's announcement in {@code k9x.event_notifications} and links it to each event it
 * applies to in the {@code k9x.events_event_notifications} join table. The announcement id is a
 * database-generated identity, so it is read back with {@code returning} to write the join rows.
 */
public class SaveEventNotificationJooqAdapter implements SaveEventNotificationPersistencePort {

    private final DSLContext dsl;

    public SaveEventNotificationJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void save(SaveEventNotificationPersistencePayload payload) {
        Long notificationId = dsl.insertInto(Tables.EVENT_NOTIFICATIONS)
                .set(Tables.EVENT_NOTIFICATIONS.TIMESTAMP, payload.timestamp())
                .set(Tables.EVENT_NOTIFICATIONS.CONTENT, payload.content())
                .returningResult(Tables.EVENT_NOTIFICATIONS.ID)
                .fetchOne(Tables.EVENT_NOTIFICATIONS.ID);
        if (notificationId == null) {
            throw new IllegalStateException("Event notification insert returned no generated id");
        }
        payload.eventIds().forEach(eventId -> dsl.insertInto(Tables.EVENTS_EVENT_NOTIFICATIONS)
                .set(Tables.EVENTS_EVENT_NOTIFICATIONS.EVENT_ID, eventId)
                .set(Tables.EVENTS_EVENT_NOTIFICATIONS.EVENT_NOTIFICATION_ID, notificationId)
                .execute());
    }
}
