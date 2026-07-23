package com.k9x.infrastructure.out.postgres.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.notifications.port.SaveNotificationPersistencePort;
import com.k9x.application.notifications.port.payload.SaveNotificationPersistencePayload;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

/**
 * Records one row per delivered notification in {@code k9x.notifications}: the recipient user, the
 * event type, a timestamp, and the free-form metadata serialized to JSON (a {@code TEXT} column). The
 * {@code id} is left unset — it is a database-generated identity.
 */
public class SaveNotificationJooqAdapter implements SaveNotificationPersistencePort {

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    public SaveNotificationJooqAdapter(DSLContext dsl, ObjectMapper objectMapper) {
        this.dsl = dsl;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(SaveNotificationPersistencePayload payload) {
        dsl.insertInto(Tables.NOTIFICATIONS)
                .set(Tables.NOTIFICATIONS.USER_ID, payload.userId())
                .set(Tables.NOTIFICATIONS.EVENT_TYPE, payload.type().name())
                .set(Tables.NOTIFICATIONS.METADATA, serialize(payload.metadata()))
                .set(Tables.NOTIFICATIONS.CREATED_AT, payload.createdAt())
                .execute();
    }

    private String serialize(Object metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize notification metadata", e);
        }
    }
}
