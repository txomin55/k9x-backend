package com.k9x.infrastructure.out.postgres.snapshot;

import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Events;
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

class GetPendingSnapshotEventsJooqAdapterTest {

    private static final Events E = Tables.EVENTS;

    @Test
    void generates_sql_filtering_finished_stages_without_snapshot() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(E.ID))};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetPendingSnapshotEventsJooqAdapter(dsl).getFinishedEventsWithoutSnapshot(1000L);

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"events\"")
                .contains("join \"k9x\".\"stages\"")
                .contains("\"k9x\".\"events\".\"deleted_at\" is null")
                .contains("\"k9x\".\"stages\".\"date_to\" < ?")
                .contains("not exists")
                .contains("\"obdx\".\"event_snapshot\"");
        assertThat(capturedBindings.get()).contains(1000L);
    }

    @Test
    void maps_result_to_event_ids() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(new org.jooq.Field<?>[]{E.ID});
            Record r1 = mockDsl.newRecord(new org.jooq.Field<?>[]{E.ID});
            r1.set(E.ID, "evt-1");
            Record r2 = mockDsl.newRecord(new org.jooq.Field<?>[]{E.ID});
            r2.set(E.ID, "evt-2");
            result.add(r1);
            result.add(r2);
            return new MockResult[]{new MockResult(2, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<String> ids = new GetPendingSnapshotEventsJooqAdapter(dsl).getFinishedEventsWithoutSnapshot(1000L);

        assertThat(ids).containsExactly("evt-1", "evt-2");
    }

    @Test
    void returns_empty_list_when_no_results() {
        MockDataProvider provider = _ -> new MockResult[]{
                new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(E.ID))
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        assertThat(new GetPendingSnapshotEventsJooqAdapter(dsl).getFinishedEventsWithoutSnapshot(1000L)).isEmpty();
    }
}
