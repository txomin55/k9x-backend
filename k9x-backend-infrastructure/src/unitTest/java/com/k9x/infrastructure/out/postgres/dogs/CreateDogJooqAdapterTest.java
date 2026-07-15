package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.domain.dogs.aggregates.Sex;
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

class CreateDogJooqAdapterTest {

    @Test
    void generates_insert_sql_with_all_fields() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(1, result)};
        };

        long createdAt = 1700000000000L;
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new CreateDogJooqAdapter(dsl).createDog("dog-123", "Rex", "img.png", "Labrador", "K9-001", "owner-1", "handler-1", "creator-1", "team-1", "ES", Sex.FEMALE, 55, null, createdAt);

        assertThat(capturedSql.get())
                .contains("insert into \"k9x\".\"dogs\"")
                .contains("\"id\"")
                .contains("\"name\"")
                .contains("\"image\"")
                .contains("\"breed\"")
                .contains("\"identity\"")
                .contains("\"owner\"")
                .contains("\"handler\"")
                .contains("\"creator\"")
                .contains("\"team\"")
                .contains("\"country\"")
                .contains("\"sex\"")
                .contains("\"withers_cm\"")
                .contains("\"created_at\"")
                .contains("\"last_update\"");
        assertThat(capturedBindings.get()).contains("dog-123", "Rex", "img.png", "Labrador", "K9-001", "owner-1", "handler-1", "creator-1", "team-1", "ES", "FEMALE", 55, createdAt);
    }

    @Test
    void upserts_on_chip_conflict_reactivating_deleted_dog() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new CreateDogJooqAdapter(dsl).createDog("dog-123", "Rex", "img.png", "Labrador", "K9-001", "owner-1", "handler-1", "creator-1", "team-1", "ES", Sex.FEMALE, 55, null, 1700000000000L);

        assertThat(capturedSql.get())
                .contains("on conflict (\"id\")")
                .contains("do update")
                .contains("\"deleted_at\" = ");
    }
}
