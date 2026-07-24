package com.k9x.infrastructure.out.postgres.notifications;

import com.k9x.application.notifications.port.GetNotificationListPersistencePort;
import com.k9x.application.notifications.use_case.dto.NotificationDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Reads one user's notifications from {@code k9x.notifications}, newest first. The stored {@code metadata}
 * (a JSON {@code TEXT} column) is returned verbatim as the DTO's {@code text}; the frontend parses it.
 */
public class GetNotificationListJooqAdapter implements GetNotificationListPersistencePort {

    private final DSLContext dsl;

    public GetNotificationListJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<NotificationDTO> getByUserId(String userId) {
        return dsl.select()
                .from(Tables.NOTIFICATIONS)
                .where(Tables.NOTIFICATIONS.USER_ID.eq(userId))
                .orderBy(Tables.NOTIFICATIONS.CREATED_AT.desc())
                .fetch(r -> new NotificationDTO(
                        String.valueOf(r.get(Tables.NOTIFICATIONS.ID)),
                        r.get(Tables.NOTIFICATIONS.CREATED_AT),
                        r.get(Tables.NOTIFICATIONS.METADATA),
                        r.get(Tables.NOTIFICATIONS.SEEN)));
    }
}
