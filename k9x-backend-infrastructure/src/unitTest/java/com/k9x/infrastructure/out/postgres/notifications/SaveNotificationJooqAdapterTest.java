package com.k9x.infrastructure.out.postgres.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.notifications.port.payload.SaveNotificationPersistencePayload;
import com.k9x.application.notifications.valueobjects.NotificationType;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class SaveNotificationJooqAdapterTest {

    @Test
    void generates_insert_sql_with_all_fields_and_serialized_metadata() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.NOTIFICATIONS.fields());
            return new MockResult[]{new MockResult(1, result)};
        };

        long createdAt = 1700000000000L;
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        SaveNotificationPersistencePayload payload = new SaveNotificationPersistencePayload(
                "creator-1", NotificationType.NEW_ENROLL, Map.of("event_id", "event-1"), createdAt);

        new SaveNotificationJooqAdapter(dsl, new ObjectMapper()).save(payload);

        assertThat(capturedSql.get())
                .contains("insert into \"k9x\".\"notifications\"")
                .contains("\"user_id\"")
                .contains("\"event_type\"")
                .contains("\"metadata\"")
                .contains("\"created_at\"");
        // id is a DB-generated identity: never written by the adapter.
        assertThat(capturedBindings.get())
                .contains("creator-1", "NEW_ENROLL", "{\"event_id\":\"event-1\"}", createdAt);
    }
}
