package com.k9x.infrastructure.out.postgres.notifications;

import com.k9x.application.notifications.port.payload.SaveEventNotificationPersistencePayload;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaveEventNotificationJooqAdapterTest {

    private static final long GENERATED_ID = 42L;

    private MockDataProvider provider(List<String> sqls, List<Object[]> bindings, boolean returnId) {
        return ctx -> {
            sqls.add(ctx.sql());
            bindings.add(ctx.bindings());
            if (ctx.sql().contains("\"event_notifications\"") && !ctx.sql().contains("events_event_notifications")) {
                Result<Record1<Long>> result =
                        DSL.using(SQLDialect.POSTGRES).newResult(Tables.EVENT_NOTIFICATIONS.ID);
                if (returnId) {
                    result.add(DSL.using(SQLDialect.POSTGRES)
                            .newRecord(Tables.EVENT_NOTIFICATIONS.ID).value1(GENERATED_ID));
                }
                return new MockResult[]{new MockResult(result.size(), result)};
            }
            return new MockResult[]{new MockResult(1, null)};
        };
    }

    @Test
    void inserts_the_announcement_and_links_it_to_every_event_with_the_generated_id() {
        List<String> sqls = new ArrayList<>();
        List<Object[]> bindings = new ArrayList<>();
        DSLContext dsl = DSL.using(new MockConnection(provider(sqls, bindings, true)), SQLDialect.POSTGRES);

        new SaveEventNotificationJooqAdapter(dsl).save(new SaveEventNotificationPersistencePayload(
                List.of("event-1", "event-2"), "Ceremony delayed", 1700000000000L));

        // One insert for the announcement, then one join row per event.
        assertThat(sqls).hasSize(3);
        assertThat(sqls.get(0))
                .contains("insert into \"k9x\".\"event_notifications\"")
                .contains("\"timestamp\"")
                .contains("\"content\"")
                .contains("returning");
        // id is a DB-generated identity: never written by the adapter.
        assertThat(sqls.get(0)).doesNotContain("\"id\",");
        assertThat(bindings.get(0)).contains(1700000000000L, "Ceremony delayed");

        assertThat(sqls.get(1)).contains("insert into \"k9x\".\"events_event_notifications\"");
        assertThat(bindings.get(1)).contains("event-1", GENERATED_ID);
        assertThat(bindings.get(2)).contains("event-2", GENERATED_ID);
    }

    @Test
    void fails_when_the_insert_returns_no_generated_id() {
        List<String> sqls = new ArrayList<>();
        List<Object[]> bindings = new ArrayList<>();
        DSLContext dsl = DSL.using(new MockConnection(provider(sqls, bindings, false)), SQLDialect.POSTGRES);

        assertThatThrownBy(() -> new SaveEventNotificationJooqAdapter(dsl).save(
                new SaveEventNotificationPersistencePayload(List.of("event-1"), "Ceremony delayed", 1L)))
                .isInstanceOf(IllegalStateException.class);

        // Nothing links to an announcement that was never identified.
        assertThat(sqls).hasSize(1);
    }
}
