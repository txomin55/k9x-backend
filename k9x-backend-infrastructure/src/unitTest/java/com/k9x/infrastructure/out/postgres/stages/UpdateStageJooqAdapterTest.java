package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.application.stages.payload.UpdateStagePersistencePayload;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
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

class UpdateStageJooqAdapterTest {

    @Test
    void generates_update_sql_with_all_fields() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.STAGES.fields());
            return new MockResult[]{new MockResult(1, result)};
        };

        long lastUpdate = 1700000000000L;
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new UpdateStageJooqAdapter(dsl).updateStage("stage-123",
                new UpdateStagePersistencePayload("New Name", 1L, 2L, lastUpdate));

        assertThat(capturedSql.get())
                .contains("update \"k9x\".\"stages\"")
                .contains("\"name\"")
                .contains("\"date_from\"")
                .contains("\"date_to\"")
                .contains("\"last_update\"")
                .contains("\"id\"");
        assertThat(capturedBindings.get()).contains("New Name", 1L, 2L, lastUpdate, "stage-123");
    }
}
