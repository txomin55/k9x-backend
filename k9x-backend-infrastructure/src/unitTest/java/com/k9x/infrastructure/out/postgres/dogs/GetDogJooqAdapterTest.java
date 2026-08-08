package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.domain.dogs.aggregates.Dog;
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

class GetDogJooqAdapterTest {

    @Test
    void generates_sql_filtered_by_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetDogJooqAdapter(dsl).getDog("dog-123");

        assertThat(capturedSql.get())
                .contains("select")
                .contains("from \"k9x\".\"dogs\"")
                .contains("\"k9x\".\"dogs\".\"identification\" = ?")
                .contains("\"k9x\".\"dogs\".\"deleted_at\" is null");
    }

    @Test
    void generates_sql_filtered_by_origin_and_not_deleted() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetDogJooqAdapter(dsl).getDogByOrigin("K9-001");

        assertThat(capturedSql.get())
                .contains("select")
                .contains("from \"k9x\".\"dogs\"")
                .contains("\"k9x\".\"dogs\".\"origin\" = ?")
                .contains("\"k9x\".\"dogs\".\"deleted_at\" is null");
    }

    @Test
    void maps_record_to_dog_domain() {
        MockDataProvider provider = _ -> {
            DSLContext ctx = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = ctx.newResult(Tables.DOGS.fields());
            Record r = ctx.newRecord(Tables.DOGS.fields());
            r.set(Tables.DOGS.IDENTIFICATION, "dog-1");
            r.set(Tables.DOGS.ORIGIN, "K9-001");
            r.set(Tables.DOGS.BREED, "Labrador");
            r.set(Tables.DOGS.NAME, "Rex");
            r.set(Tables.DOGS.IMAGE, "img.png");
            r.set(Tables.DOGS.OWNER, "owner-1");
            r.set(Tables.DOGS.HANDLER, "handler-1");
            r.set(Tables.DOGS.CREATOR, "creator-1");
            r.set(Tables.DOGS.COUNTRY, "ES");
            r.set(Tables.DOGS.TEAM, "team-1");
            r.set(Tables.DOGS.SEX, "FEMALE");
            r.set(Tables.DOGS.WITHERS_CM, 55);
            r.set(Tables.DOGS.LAST_UPDATE, 1000L);
            r.set(Tables.DOGS.CREATED_AT, 500L);
            r.set(Tables.DOGS.DELETED_AT, null);
            result.add(r);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Dog dog = new GetDogJooqAdapter(dsl).getDog("dog-1");

        assertThat(dog).isNotNull();
        assertThat(dog.identification()).isEqualTo("dog-1");
        assertThat(dog.origin()).isEqualTo("K9-001");
        assertThat(dog.breed()).isEqualTo("Labrador");
        assertThat(dog.name()).isEqualTo("Rex");
        assertThat(dog.image()).isEqualTo("img.png");
        assertThat(dog.owner()).isEqualTo("owner-1");
        assertThat(dog.handler()).isEqualTo("handler-1");
        assertThat(dog.creator()).isEqualTo("creator-1");
        assertThat(dog.country()).isEqualTo("ES");
        assertThat(dog.team()).isEqualTo("team-1");
        assertThat(dog.getSex()).isEqualTo(Sex.FEMALE);
        assertThat(dog.getWithersCm()).isEqualTo(55);
        assertThat(dog.lastUpdate()).isEqualTo(1000L);
        assertThat(dog.createdAt()).isEqualTo(500L);
        assertThat(dog.deletedAt()).isNull();
    }

    @Test
    void returns_null_when_dog_not_found() {
        MockDataProvider provider = _ -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Dog dog = new GetDogJooqAdapter(dsl).getDog("nonexistent");

        assertThat(dog).isNull();
    }
}
