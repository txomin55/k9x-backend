package com.k9x.infrastructure.out.postgres.notifications;

import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetEventRecipientsJooqAdapterTest {

    private MockDataProvider provider(AtomicReference<String> capturedSql,
                                      AtomicReference<Object[]> capturedBindings,
                                      String... owners) {
        return ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record1<String>> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.OWNER);
            for (String owner : owners) {
                result.add(DSL.using(SQLDialect.POSTGRES).newRecord(Tables.DOGS.OWNER).value1(owner));
            }
            return new MockResult[]{new MockResult(result.size(), result)};
        };
    }

    @Test
    void selects_distinct_dog_owners_of_the_events_excluding_deleted_and_ownerless() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();
        DSLContext dsl = DSL.using(new MockConnection(
                provider(capturedSql, capturedBindings, "owner-1", "owner-2")), SQLDialect.POSTGRES);

        Set<String> recipients = new GetEventRecipientsJooqAdapter(dsl)
                .getRecipientIds(List.of("event-1", "event-2"));

        assertThat(recipients).containsExactly("owner-1", "owner-2");
        assertThat(capturedSql.get())
                .contains("select distinct")
                .contains("\"k9x\".\"dogs\".\"owner\"")
                .contains("\"obdx\".\"event_competitors\"")
                .contains("\"k9x\".\"dogs\".\"deleted_at\" is null")
                .contains("\"k9x\".\"dogs\".\"owner\" is not null")
                .contains("\"k9x\".\"events\".\"deleted_at\" is null");
        assertThat(capturedBindings.get()).contains("event-1", "event-2");
    }

    @Test
    void unions_the_users_subscribed_to_any_of_the_events() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();
        DSLContext dsl = DSL.using(new MockConnection(
                provider(capturedSql, capturedBindings, "owner-1", "subscriber-1")), SQLDialect.POSTGRES);

        Set<String> recipients = new GetEventRecipientsJooqAdapter(dsl)
                .getRecipientIds(List.of("event-1", "event-2"));

        assertThat(recipients).containsExactly("owner-1", "subscriber-1");
        assertThat(capturedSql.get())
                .contains("union")
                .contains("\"k9x\".\"user_subscriptions\".\"user_id\"")
                .contains("\"k9x\".\"user_subscriptions\".\"event_ids\" && cast(? as varchar(255)[])");
        // The overlap operand is bound as a single Postgres array value, rendered as {event-1,event-2}.
        assertThat(String.valueOf(capturedBindings.get()[capturedBindings.get().length - 1]))
                .contains("event-1", "event-2");
    }

    @Test
    void deduplicates_an_owner_with_dogs_in_several_events() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();
        // A DISTINCT query cannot return the same owner twice, but the adapter's Set must collapse it anyway.
        DSLContext dsl = DSL.using(new MockConnection(
                provider(capturedSql, capturedBindings, "owner-1", "owner-1")), SQLDialect.POSTGRES);

        Set<String> recipients = new GetEventRecipientsJooqAdapter(dsl)
                .getRecipientIds(List.of("event-1", "event-2"));

        assertThat(recipients).containsExactly("owner-1");
    }

    @Test
    void returns_no_recipients_and_hits_no_database_when_there_are_no_events() {
        DSLContext dsl = DSL.using(new MockConnection(ctx -> {
            throw new AssertionError("no query expected for an empty event list");
        }), SQLDialect.POSTGRES);

        assertThat(new GetEventRecipientsJooqAdapter(dsl).getRecipientIds(List.of())).isEmpty();
        assertThat(new GetEventRecipientsJooqAdapter(dsl).getRecipientIds(null)).isEmpty();
    }
}
