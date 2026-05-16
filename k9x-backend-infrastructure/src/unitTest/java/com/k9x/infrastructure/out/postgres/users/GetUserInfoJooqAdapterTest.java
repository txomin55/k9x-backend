package com.k9x.infrastructure.out.postgres.users;

import com.k9x.application.users.dto.UserInfoDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetUserInfoJooqAdapterTest {

    private static final Field<?>[] FIELDS = {Tables.USERS.ID, Tables.USERS.EMAIL, Tables.ORGANIZERS.USER_ID};

    @Test
    void generates_sql_with_left_join_filtered_by_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetUserInfoJooqAdapter(dsl).findById("user-123");

        assertThat(capturedSql.get())
                .contains("select \"k9x\".\"users\".\"id\", \"k9x\".\"users\".\"email\", \"k9x\".\"organizers\".\"user_id\"")
                .contains("from \"k9x\".\"users\"")
                .contains("left outer join \"k9x\".\"organizers\" on \"k9x\".\"organizers\".\"user_id\" = \"k9x\".\"users\".\"id\"")
                .contains("where \"k9x\".\"users\".\"id\" = ?");
        assertThat(capturedBindings.get()).containsExactly("user-123");
    }

    @Test
    void maps_record_to_user_info_dto() {
        MockDataProvider provider = ctx -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(FIELDS);
            Record record = mockDsl.newRecord(FIELDS);
            record.set(Tables.USERS.ID, "user-123");
            record.set(Tables.USERS.EMAIL, "user@example.com");
            record.set(Tables.ORGANIZERS.USER_ID, "user-123");
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        UserInfoDTO dto = new GetUserInfoJooqAdapter(dsl).findById("user-123");

        assertThat(dto.getId()).isEqualTo("user-123");
        assertThat(dto.getEmail()).isEqualTo("user@example.com");
        assertThat(dto.isOrganizer()).isTrue();
    }

    @Test
    void returns_null_when_user_not_found() {
        MockDataProvider provider = ctx -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        UserInfoDTO dto = new GetUserInfoJooqAdapter(dsl).findById("user-123");

        assertThat(dto).isNull();
    }
}
