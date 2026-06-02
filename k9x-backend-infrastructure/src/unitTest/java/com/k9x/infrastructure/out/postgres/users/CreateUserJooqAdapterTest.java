package com.k9x.infrastructure.out.postgres.users;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserJooqAdapterTest {

    @Test
    void generates_insert_with_email_as_id_and_provided_image() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult();
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new CreateUserJooqAdapter(dsl).createUser("user@example.com", "http://img/u.png");

        assertThat(capturedSql.get())
                .contains("insert into \"k9x\".\"users\"")
                .contains("\"id\"")
                .contains("\"email\"")
                .contains("\"image\"");
        assertThat(capturedBindings.get()).containsExactly("user@example.com", "user@example.com", "http://img/u.png");
    }
}
