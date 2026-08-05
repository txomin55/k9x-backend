package com.k9x.infrastructure.out.postgres.subscriptions;

import com.k9x.application.subscriptions.port.payload.UpdateUserSubscriptionPersistencePayload;
import com.k9x.domain.subscriptions.SubscriptionKind;
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

class UpdateUserSubscriptionJooqAdapterTest {

    @Test
    void appends_every_event_id_without_duplicating_them_when_subscribing() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        DSLContext dsl = DSL.using(new MockConnection(capturing(capturedSql, capturedBindings)), SQLDialect.POSTGRES);
        new UpdateUserSubscriptionJooqAdapter(dsl).updateUserSubscription(
                new UpdateUserSubscriptionPersistencePayload("user@example.com", SubscriptionKind.EVENT,
                        List.of("event-1", "event-2"), true));

        assertThat(capturedSql.get())
                .contains("update \"k9x\".\"user_subscriptions\"")
                .contains("set \"event_ids\" = array_cat(array_remove(array_remove("
                        + "\"k9x\".\"user_subscriptions\".\"event_ids\", ?), ?), cast(? as varchar(255)[]))")
                .contains("where \"k9x\".\"user_subscriptions\".\"user_id\" = ?");
        assertThat(capturedBindings.get()).hasSize(4);
        assertThat(capturedBindings.get()[0]).isEqualTo("event-1");
        assertThat(capturedBindings.get()[1]).isEqualTo("event-2");
        // The appended set is bound as a single Postgres array value, rendered as {event-1,event-2}.
        assertThat(String.valueOf(capturedBindings.get()[2])).contains("event-1", "event-2");
        assertThat(capturedBindings.get()[3]).isEqualTo("user@example.com");
    }

    @Test
    void removes_every_event_id_when_unsubscribing() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        DSLContext dsl = DSL.using(new MockConnection(capturing(capturedSql, capturedBindings)), SQLDialect.POSTGRES);
        new UpdateUserSubscriptionJooqAdapter(dsl).updateUserSubscription(
                new UpdateUserSubscriptionPersistencePayload("user@example.com", SubscriptionKind.EVENT,
                        List.of("event-1", "event-2"), false));

        assertThat(capturedSql.get())
                .contains("set \"event_ids\" = array_remove(array_remove("
                        + "\"k9x\".\"user_subscriptions\".\"event_ids\", ?), ?)")
                .doesNotContain("array_cat")
                .contains("where \"k9x\".\"user_subscriptions\".\"user_id\" = ?");
        assertThat(capturedBindings.get()).containsExactly("event-1", "event-2", "user@example.com");
    }

    private static MockDataProvider capturing(AtomicReference<String> sql, AtomicReference<Object[]> bindings) {
        return ctx -> {
            sql.set(ctx.sql());
            bindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
            return new MockResult[]{new MockResult(1, result)};
        };
    }
}
