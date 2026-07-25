package com.k9x.infrastructure.out.postgres.notifications;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.notifications.port.GetNotificationListPersistencePort;
import com.k9x.application.notifications.use_case.dto.NotificationDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Reads one user's notifications from {@code k9x.notifications}, newest first. The stored {@code metadata}
 * (a JSON {@code TEXT} column) is deserialized back into a string/string map for the read model.
 */
public class GetNotificationListJooqAdapter implements GetNotificationListPersistencePort {

    private static final TypeReference<Map<String, String>> METADATA_TYPE = new TypeReference<>() {
    };

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    public GetNotificationListJooqAdapter(DSLContext dsl, ObjectMapper objectMapper) {
        this.dsl = dsl;
        this.objectMapper = objectMapper;
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
                        r.get(Tables.NOTIFICATIONS.EVENT_TYPE),
                        deserialize(r.get(Tables.NOTIFICATIONS.METADATA)),
                        r.get(Tables.NOTIFICATIONS.SEEN)));
    }

    private Map<String, String> deserialize(String metadata) {
        try {
            return objectMapper.readValue(metadata, METADATA_TYPE);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to deserialize notification metadata", e);
        }
    }
}
