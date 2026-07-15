package com.k9x.infrastructure.out.postgres.events;

import com.k9x.application.events.snapshot.use_case.dto.PendingSnapshotEventDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Events;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetPendingSnapshotEventsJooqAdapterTest {

    private static final Events E = Tables.EVENTS;
    private static final Field<?>[] FIELDS = {E.ID, E.DISCIPLINE};

    @Test
    void generates_sql_filtering_finished_stages_without_snapshot() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(FIELDS))};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetPendingSnapshotEventsJooqAdapter(dsl).getFinishedEventsWithoutSnapshot(1000L);

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"events\"")
                .contains("join \"k9x\".\"stages\"")
                .contains("\"k9x\".\"events\".\"discipline\"")
                .contains("\"k9x\".\"events\".\"deleted_at\" is null")
                .contains("\"k9x\".\"stages\".\"date_to\" < ?")
                .contains("not exists")
                .contains("\"obdx\".\"event_snapshot\"");
        assertThat(capturedBindings.get()).contains(1000L);
    }

    @Test
    void maps_result_to_pending_events_with_discipline() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            Record r1 = mockDsl.newRecord(FIELDS);
            r1.set(E.ID, "evt-1");
            r1.set(E.DISCIPLINE, "obdx");
            Record r2 = mockDsl.newRecord(FIELDS);
            r2.set(E.ID, "evt-2");
            r2.set(E.DISCIPLINE, "obdx");
            result.add(r1);
            result.add(r2);
            return new MockResult[]{new MockResult(2, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<PendingSnapshotEventDTO> pending =
                new GetPendingSnapshotEventsJooqAdapter(dsl).getFinishedEventsWithoutSnapshot(1000L);

        assertThat(pending).containsExactly(
                new PendingSnapshotEventDTO("evt-1", "obdx"),
                new PendingSnapshotEventDTO("evt-2", "obdx"));
    }

    @Test
    void returns_empty_list_when_no_results() {
        MockDataProvider provider = _ -> new MockResult[]{
                new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(FIELDS))
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        assertThat(new GetPendingSnapshotEventsJooqAdapter(dsl).getFinishedEventsWithoutSnapshot(1000L)).isEmpty();
    }
}
