package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.port.payload.UpdateObdxScorePersistencePayload;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateObdxScoreJooqAdapterTest {

    @Test
    void generates_upsert_sql() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.EVENT_SCORES.fields());
            return new MockResult[]{new MockResult(1, result)};
        };

        long lastUpdate = 1700000000000L;
        BigDecimal score = new BigDecimal("7.5");
        UpdateObdxScorePersistencePayload payload = new UpdateObdxScorePersistencePayload(
                "judge-1", "exercise-1", "dog-1", score, lastUpdate);
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);

        new UpdateObdxScoreJooqAdapter(dsl).updateScore("event-1", payload);

        assertThat(capturedSql.get())
                .contains("insert into \"obdx\".\"event_scores\"")
                .contains("on conflict")
                .contains("\"score\"");
        assertThat(capturedBindings.get())
                .contains("event-1", "exercise-1", "judge-1", "dog-1", score, lastUpdate);
    }
}
