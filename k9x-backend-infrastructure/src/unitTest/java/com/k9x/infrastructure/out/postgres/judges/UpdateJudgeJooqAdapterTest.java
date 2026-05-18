package com.k9x.infrastructure.out.postgres.judges;

import com.k9x.application.judges.payload.UpdateJudgePersistencePayload;
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

class UpdateJudgeJooqAdapterTest {

    @Test
    void generates_update_sql_setting_name_and_last_update_filtered_by_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.JUDGES.fields());
            return new MockResult[]{new MockResult(1, result)};
        };

        long lastUpdate = 1700000000000L;
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new UpdateJudgeJooqAdapter(dsl).updateJudge("judge-123",
                new UpdateJudgePersistencePayload("NewName", lastUpdate));

        assertThat(capturedSql.get())
                .contains("update \"k9x\".\"judges\"")
                .contains("\"name\" = ?")
                .contains("\"last_update\" = ?")
                .contains("where \"k9x\".\"judges\".\"id\" = ?");
        assertThat(capturedBindings.get()).contains("NewName", lastUpdate, "judge-123");
    }
}
