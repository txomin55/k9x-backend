package com.k9x.infrastructure.out.postgres.events.obdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventSnapshot;
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

class SaveObdxEventSnapshotJooqAdapterTest {

    private static final EventSnapshot ES = EventSnapshot.EVENT_SNAPSHOT;

    private FetchClassificationDTO classification() {
        return new FetchClassificationDTO("evt-1", "Event", "FINISHED", "stage-1", "Stage A", "WC",
                "obdx", "cfg", "Cfg", 5000L,
                new FetchObdxClassificationDTO(5000L, List.of(), "AVG", List.of()));
    }

    @Test
    void generates_insert_with_on_conflict_do_nothing_and_serialized_snapshot() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(ES.fields());
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new SaveObdxEventSnapshotJooqAdapter(dsl, new ObjectMapper()).save("evt-1", 1700000000000L, classification());

        assertThat(capturedSql.get())
                .contains("insert into \"obdx\".\"event_snapshot\"")
                .contains("\"event_id\"")
                .contains("\"timestamp\"")
                .contains("\"snapshot\"")
                .contains("on conflict")
                .contains("do nothing");
        assertThat(capturedBindings.get()).contains("evt-1", 1700000000000L);
        assertThat(capturedBindings.get()).anyMatch(b -> b != null && b.toString().contains("\"eventId\":\"evt-1\""));
    }
}
