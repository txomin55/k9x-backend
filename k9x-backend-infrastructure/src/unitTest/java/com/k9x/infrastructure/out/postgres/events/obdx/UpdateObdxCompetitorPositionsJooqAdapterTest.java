package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.snapshot.port.payload.ObdxCompetitorPosition;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateObdxCompetitorPositionsJooqAdapterTest {

    private static final EventCompetitors EC = EventCompetitors.EVENT_COMPETITORS;

    @Test
    void generates_a_batched_position_update_filtered_by_event_and_dog() {
        AtomicReference<String[]> capturedBatchSql = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            String[] sql = ctx.batch() ? ctx.batchSQL() : new String[]{ctx.sql()};
            capturedBatchSql.set(sql);
            Result<Record> empty = DSL.using(SQLDialect.POSTGRES).newResult(EC.fields());
            MockResult[] out = new MockResult[sql.length];
            for (int i = 0; i < out.length; i++) {
                out[i] = new MockResult(1, empty);
            }
            return out;
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new UpdateObdxCompetitorPositionsJooqAdapter(dsl).updatePositions("evt-1",
                List.of(new ObdxCompetitorPosition("dog-1", (short) 1),
                        new ObdxCompetitorPosition("dog-2", (short) 3)));

        String[] batchSql = capturedBatchSql.get();
        assertThat(batchSql).hasSize(2);
        assertThat(batchSql[0])
                .contains("update \"obdx\".\"event_competitors\"")
                .contains("set \"position\"")
                .contains("\"event_id\"")
                .contains("\"dog_id\"");
    }

    @Test
    void does_not_touch_the_database_when_there_are_no_positions() {
        AtomicBoolean queried = new AtomicBoolean(false);
        MockDataProvider provider = ctx -> {
            queried.set(true);
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(EC.fields()))};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new UpdateObdxCompetitorPositionsJooqAdapter(dsl).updatePositions("evt-1", List.of());

        assertThat(queried).isFalse();
    }
}
