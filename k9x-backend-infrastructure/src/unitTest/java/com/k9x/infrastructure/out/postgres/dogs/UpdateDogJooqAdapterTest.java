package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.payload.UpdateDogPersistencePayload;
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

class UpdateDogJooqAdapterTest {

    @Test
    void generates_update_sql_setting_all_fields_filtered_by_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(1, result)};
        };

        long lastUpdate = 1700000000000L;
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new UpdateDogJooqAdapter(dsl).updateDog("dog-123",
                new UpdateDogPersistencePayload("Rex", "img.png", "Labrador", "K9-001", "owner-1", "handler-1", "team-1", "ES", Sex.FEMALE, 55, lastUpdate));

        assertThat(capturedSql.get())
                .contains("update \"k9x\".\"dogs\"")
                .contains("\"name\" = ?")
                .contains("\"image\" = ?")
                .contains("\"breed\" = ?")
                .contains("\"identity\" = ?")
                .contains("\"owner\" = ?")
                .contains("\"handler\" = ?")
                .contains("\"team\" = ?")
                .contains("\"country\" = ?")
                .contains("\"sex\" = ?")
                .contains("\"withers_cm\" = ?")
                .contains("\"last_update\" = ?")
                .contains("where \"k9x\".\"dogs\".\"id\" = ?");
        assertThat(capturedBindings.get()).contains("dog-123", "Rex", "img.png", "Labrador", "K9-001", "owner-1", "handler-1", "team-1", "ES", "FEMALE", 55, lastUpdate);
    }
}
