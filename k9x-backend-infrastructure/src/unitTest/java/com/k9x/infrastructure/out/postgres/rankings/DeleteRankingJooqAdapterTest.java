package com.k9x.infrastructure.out.postgres.rankings;

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

class DeleteRankingJooqAdapterTest {

    @Test
    void generates_a_physical_delete_filtered_by_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new DeleteRankingJooqAdapter(dsl).deleteRanking("ranking_comp-1");

        // Rankings have no soft delete, so this must never render as an update of deleted_at.
        assertThat(capturedSql.get())
                .contains("delete from \"k9x\".\"rankings\"")
                .contains("\"id\" = ?")
                .doesNotContain("update")
                .doesNotContain("deleted_at");
        assertThat(capturedBindings.get()).containsExactly("ranking_comp-1");
    }
}
