package com.k9x.infrastructure.out.postgres.notifications;

import com.k9x.application.notifications.port.MarkNotificationsSeenPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Flips {@code seen} to {@code true} for the given notification ids, always scoped to the owning
 * {@code user_id} so a user can only mark their own notifications. Non-numeric ids are ignored (the
 * database id is a {@code BIGINT} identity).
 */
public class MarkNotificationsSeenJooqAdapter implements MarkNotificationsSeenPersistencePort {

    private final DSLContext dsl;

    public MarkNotificationsSeenJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void markSeen(String userId, List<String> notificationIds) {
        List<Long> ids = notificationIds.stream()
                .map(this::toLongOrNull)
                .filter(id -> id != null)
                .toList();
        if (ids.isEmpty()) {
            return;
        }
        dsl.update(Tables.NOTIFICATIONS)
                .set(Tables.NOTIFICATIONS.SEEN, true)
                .where(Tables.NOTIFICATIONS.USER_ID.eq(userId))
                .and(Tables.NOTIFICATIONS.ID.in(ids))
                .execute();
    }

    private Long toLongOrNull(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
