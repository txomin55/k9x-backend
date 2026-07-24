package com.k9x.infrastructure.out.postgres.notifications;

import com.k9x.application.notifications.use_case.dto.NotificationDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.records.NotificationsRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetNotificationListJooqAdapterTest {

    @Test
    void generates_sql_scoped_by_user_ordered_newest_first() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.NOTIFICATIONS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetNotificationListJooqAdapter(dsl).getByUserId("creator-1");

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"notifications\"")
                .contains("where \"k9x\".\"notifications\".\"user_id\" = ?")
                .contains("order by \"k9x\".\"notifications\".\"created_at\" desc");
        assertThat(capturedBindings.get()).containsExactly("creator-1");
    }

    @Test
    void maps_row_to_dto_with_metadata_as_text_and_id_as_string() {
        MockDataProvider provider = ctx -> {
            Result<NotificationsRecord> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.NOTIFICATIONS);
            NotificationsRecord record = DSL.using(SQLDialect.POSTGRES).newRecord(Tables.NOTIFICATIONS);
            record.setId(42L);
            record.setUserId("creator-1");
            record.setEventType("NEW_ENROLL");
            record.setMetadata("{\"event_id\":\"event-1\"}");
            record.setCreatedAt(1700000000000L);
            record.setSeen(false);
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<NotificationDTO> notifications = new GetNotificationListJooqAdapter(dsl).getByUserId("creator-1");

        assertThat(notifications).hasSize(1);
        NotificationDTO dto = notifications.get(0);
        assertThat(dto.id()).isEqualTo("42");
        assertThat(dto.timestamp()).isEqualTo(1700000000000L);
        assertThat(dto.text()).isEqualTo("{\"event_id\":\"event-1\"}");
        assertThat(dto.seen()).isFalse();
    }
}
