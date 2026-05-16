package com.k9x.infrastructure.out.postgres.dogs;

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

class DeleteDogJooqAdapterTest {

    @Test
    void generates_update_sql_setting_deleted_at_filtered_by_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(1, result)};
        };

        long deletedAt = 1700000000000L;
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new DeleteDogJooqAdapter(dsl).deleteDog("dog-123", deletedAt);

        assertThat(capturedSql.get())
                .contains("update \"k9x\".\"dogs\"")
                .contains("set \"deleted_at\" = ?")
                .contains("where \"k9x\".\"dogs\".\"id\" = ?");
        assertThat(capturedBindings.get()[0]).isEqualTo(deletedAt);
        assertThat(capturedBindings.get()[1]).isEqualTo("dog-123");
    }
}
