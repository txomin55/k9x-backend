package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.domain.aggregates.dogs.Dog;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetDogListJooqAdapterTest {

    @Test
    void generates_sql_filtered_by_owner() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetDogListJooqAdapter(dsl).getDogs("owner-123");

        assertThat(capturedSql.get())
                .contains("""
                        select "k9x"."dogs"."id", "k9x"."dogs"."identity", "k9x"."dogs"."breed", \
                        "k9x"."dogs"."name", "k9x"."dogs"."image", "k9x"."dogs"."owner", \
                        "k9x"."dogs"."creator", "k9x"."dogs"."country", "k9x"."dogs"."team", \
                        "k9x"."dogs"."last_update", "k9x"."dogs"."created_at", "k9x"."dogs"."deleted_at"\
                        """)
                .contains("from \"k9x\".\"dogs\"")
                .contains("where ((\"k9x\".\"dogs\".\"owner\" = ? or \"k9x\".\"dogs\".\"creator\" = ?) and \"k9x\".\"dogs\".\"deleted_at\" is null)");
        assertThat(capturedBindings.get()).containsExactly("owner-123", "owner-123");
    }

    @Test
    void maps_records_to_dog_domain() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(Tables.DOGS.fields());
            Record record = mockDsl.newRecord(Tables.DOGS.fields());
            record.set(Tables.DOGS.ID, "id-1");
            record.set(Tables.DOGS.IDENTITY, "ident-1");
            record.set(Tables.DOGS.BREED, "Labrador");
            record.set(Tables.DOGS.NAME, "Rex");
            record.set(Tables.DOGS.IMAGE, "img.png");
            record.set(Tables.DOGS.OWNER, "owner-123");
            record.set(Tables.DOGS.CREATOR, "creator-1");
            record.set(Tables.DOGS.COUNTRY, "ES");
            record.set(Tables.DOGS.TEAM, "team-1");
            record.set(Tables.DOGS.LAST_UPDATE, 1000L);
            record.set(Tables.DOGS.CREATED_AT, 2000L);
            record.set(Tables.DOGS.DELETED_AT, 0L);
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<Dog> dogs = new GetDogListJooqAdapter(dsl).getDogs("owner-123");

        assertThat(dogs).hasSize(1);
        Dog dog = dogs.getFirst();
        assertThat(dog.id()).isEqualTo("id-1");
        assertThat(dog.identity()).isEqualTo("ident-1");
        assertThat(dog.breed()).isEqualTo("Labrador");
        assertThat(dog.name()).isEqualTo("Rex");
        assertThat(dog.image()).isEqualTo("img.png");
        assertThat(dog.owner()).isEqualTo("owner-123");
        assertThat(dog.creator()).isEqualTo("creator-1");
        assertThat(dog.country()).isEqualTo("ES");
        assertThat(dog.team()).isEqualTo("team-1");
        assertThat(dog.lastUpdate()).isEqualTo(1000L);
        assertThat(dog.createdAt()).isEqualTo(2000L);
        assertThat(dog.deletedAt()).isEqualTo(0L);
    }
}
