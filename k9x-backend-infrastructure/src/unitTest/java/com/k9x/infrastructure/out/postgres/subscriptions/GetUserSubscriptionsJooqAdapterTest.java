package com.k9x.infrastructure.out.postgres.subscriptions;

import com.k9x.application.subscriptions.use_case.dto.UserSubscriptionsDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetUserSubscriptionsJooqAdapterTest {

    private static final Field<?>[] FIELDS = {Tables.USER_SUBSCRIPTIONS.EVENT_IDS};

    @Test
    void generates_select_filtered_by_user_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetUserSubscriptionsJooqAdapter(dsl).getUserSubscriptions("user@example.com");

        assertThat(capturedSql.get())
                .contains("select \"k9x\".\"user_subscriptions\".\"event_ids\"")
                .contains("from \"k9x\".\"user_subscriptions\"")
                .contains("where \"k9x\".\"user_subscriptions\".\"user_id\" = ?");
        assertThat(capturedBindings.get()).containsExactly("user@example.com");
    }

    @Test
    void maps_the_event_ids_array_to_the_dto_list() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            Record record = mockDsl.newRecord(FIELDS);
            record.set(Tables.USER_SUBSCRIPTIONS.EVENT_IDS, new String[]{"event-1", "event-2"});
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        UserSubscriptionsDTO dto = new GetUserSubscriptionsJooqAdapter(dsl).getUserSubscriptions("user@example.com");

        assertThat(dto.eventIds()).containsExactly("event-1", "event-2");
    }

    @Test
    void returns_empty_subscriptions_when_the_user_has_no_record() {
        MockDataProvider provider = _ -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        UserSubscriptionsDTO dto = new GetUserSubscriptionsJooqAdapter(dsl).getUserSubscriptions("user@example.com");

        assertThat(dto.eventIds()).isEmpty();
    }
}
