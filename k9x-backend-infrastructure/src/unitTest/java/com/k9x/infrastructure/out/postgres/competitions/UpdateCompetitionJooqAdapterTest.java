package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.payload.UpdateCompetitionPersistencePayload;
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

class UpdateCompetitionJooqAdapterTest {

    @Test
    void generates_update_sql_with_all_fields() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.COMPETITIONS.fields());
            return new MockResult[]{new MockResult(1, result)};
        };

        long lastUpdate = 1700000000000L;
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new UpdateCompetitionJooqAdapter(dsl).updateCompetition("comp-123",
                new UpdateCompetitionPersistencePayload("World Cup", "A great competition", "ES", "Madrid, Spain", 40.4168, -3.7038, lastUpdate));

        assertThat(capturedSql.get())
                .contains("update \"k9x\".\"competitions\"")
                .contains("\"name\"")
                .contains("\"description\"")
                .contains("\"country\"")
                .contains("\"address\"")
                .contains("\"coord_alt\"")
                .contains("\"coord_long\"")
                .contains("\"last_update\"")
                .contains("\"id\"");
        assertThat(capturedBindings.get()).contains("comp-123", "World Cup", "A great competition",
                "ES", "Madrid, Spain", 40.4168, -3.7038, lastUpdate);
    }
}
