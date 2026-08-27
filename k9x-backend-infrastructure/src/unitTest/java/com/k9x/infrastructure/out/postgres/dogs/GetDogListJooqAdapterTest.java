package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.payload.DogListFilter;
import com.k9x.application.dogs.port.payload.DogListPage;
import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
        new GetDogListJooqAdapter(dsl).getDogs(filter("owner-123", null));

        assertThat(capturedSql.get())
                .contains("""
                        select "k9x"."dogs"."identification", "k9x"."dogs"."origin", "k9x"."dogs"."license", "k9x"."dogs"."breed", \
                        "k9x"."dogs"."name", "k9x"."dogs"."image", "k9x"."dogs"."owner", \
                        "k9x"."dogs"."creator", "k9x"."dogs"."country", "k9x"."dogs"."team", \
                        "k9x"."dogs"."last_update", "k9x"."dogs"."created_at", "k9x"."dogs"."deleted_at", \
                        "k9x"."dogs"."handler"\
                        """)
                .contains("from \"k9x\".\"dogs\"")
                .contains("where (\"k9x\".\"dogs\".\"owner\" = ? "
                        + "and \"k9x\".\"dogs\".\"deleted_at\" is null)");
        assertThat(capturedBindings.get()).containsExactly("owner-123");
    }

    @Test
    void generates_sql_filtered_by_creator() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetDogListJooqAdapter(dsl).getDogs(filter(null, "creator-123"));

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"dogs\"")
                .contains("where (\"k9x\".\"dogs\".\"creator\" = ? "
                        + "and \"k9x\".\"dogs\".\"deleted_at\" is null)");
        assertThat(capturedBindings.get()).containsExactly("creator-123");
    }

    @Test
    void generates_sql_merging_owner_and_creator() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetDogListJooqAdapter(dsl).getDogs(filter("user-123", "user-123"));

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"dogs\"")
                .contains("where ((\"k9x\".\"dogs\".\"owner\" = ? "
                        + "or \"k9x\".\"dogs\".\"creator\" = ?) "
                        + "and \"k9x\".\"dogs\".\"deleted_at\" is null)");
        assertThat(capturedBindings.get()).containsExactly("user-123", "user-123");
    }

    @Test
    void generates_sql_without_owner_filter_when_owner_is_null() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetDogListJooqAdapter(dsl).getDogs(filter(null, null));

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"dogs\"")
                .contains("\"k9x\".\"dogs\".\"deleted_at\" is null")
                .doesNotContain("\"owner\" = ?")
                .doesNotContain("\"creator\" = ?");
        assertThat(capturedBindings.get()).isEmpty();
    }

    @Test
    void maps_records_to_dog_domain() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(Tables.DOGS.fields());
            Record record = mockDsl.newRecord(Tables.DOGS.fields());
            record.set(Tables.DOGS.IDENTIFICATION, "id-1");
            record.set(Tables.DOGS.ORIGIN, "ident-1");
            record.set(Tables.DOGS.BREED, "Labrador");
            record.set(Tables.DOGS.NAME, "Rex");
            record.set(Tables.DOGS.IMAGE, "img.png");
            record.set(Tables.DOGS.OWNER, "owner-123");
            record.set(Tables.DOGS.HANDLER, "handler-1");
            record.set(Tables.DOGS.CREATOR, "creator-1");
            record.set(Tables.DOGS.COUNTRY, "ES");
            record.set(Tables.DOGS.TEAM, "team-1");
            record.set(Tables.DOGS.SEX, "MALE");
            record.set(Tables.DOGS.WITHERS_CM, 60);
            record.set(Tables.DOGS.LAST_UPDATE, 1000L);
            record.set(Tables.DOGS.CREATED_AT, 2000L);
            record.set(Tables.DOGS.DELETED_AT, null);
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        DogListPage page = new GetDogListJooqAdapter(dsl).getDogs(filter("owner-123", null));

        assertThat(page.dogs()).hasSize(1);
        assertThat(page.total()).isEqualTo(1);
        Dog dog = page.dogs().getFirst();
        assertThat(dog.identification()).isEqualTo("id-1");
        assertThat(dog.origin()).isEqualTo("ident-1");
        assertThat(dog.breed()).isEqualTo("Labrador");
        assertThat(dog.name()).isEqualTo("Rex");
        assertThat(dog.image()).isEqualTo("img.png");
        assertThat(dog.owner()).isEqualTo("owner-123");
        assertThat(dog.handler()).isEqualTo("handler-1");
        assertThat(dog.creator()).isEqualTo("creator-1");
        assertThat(dog.country()).isEqualTo("ES");
        assertThat(dog.team()).isEqualTo("team-1");
        assertThat(dog.getSex()).isEqualTo(Sex.MALE);
        assertThat(dog.getWithersCm()).isEqualTo(60);
        assertThat(dog.lastUpdate()).isEqualTo(1000L);
        assertThat(dog.createdAt()).isEqualTo(2000L);
        assertThat(dog.deletedAt()).isNull();
    }

    // ---- name filter & pagination -----------------------------------------------------------------

    @Test
    void generates_case_insensitive_name_search() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetDogListJooqAdapter(dsl).getDogs(new DogListFilter("owner-123", null, "re", null, null));

        assertThat(capturedSql.get()).contains("lower(\"k9x\".\"dogs\".\"name\") like");
        assertThat(capturedBindings.get()).contains("re");
    }

    @Test
    void orders_by_name_so_pages_are_stable() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetDogListJooqAdapter(dsl).getDogs(filter(null, null));

        assertThat(capturedSql.get())
                .contains("order by \"k9x\".\"dogs\".\"name\" asc, \"k9x\".\"dogs\".\"identification\" asc");
    }

    @Test
    void generates_limit_and_offset_and_counts_the_total_when_paginated() {
        List<String> capturedSql = new ArrayList<>();

        MockDataProvider provider = ctx -> {
            capturedSql.add(ctx.sql());
            if (ctx.sql().contains("count(*)")) {
                Result<Record1<Integer>> count = DSL.using(SQLDialect.POSTGRES).newResult(DSL.count());
                Record1<Integer> record = DSL.using(SQLDialect.POSTGRES).newRecord(DSL.count());
                record.value1(137);
                count.add(record);
                return new MockResult[]{new MockResult(1, count)};
            }
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        DogListPage page = new GetDogListJooqAdapter(dsl).getDogs(new DogListFilter(null, null, null, 40, 20));

        assertThat(capturedSql).anyMatch(sql -> sql.contains("count(*)"));
        assertThat(capturedSql).anyMatch(sql -> sql.contains("offset ?") && sql.contains("fetch next ? rows only"));
        // The total is the whole match count, not the size of the returned page.
        assertThat(page.total()).isEqualTo(137);
        assertThat(page.dogs()).isEmpty();
    }

    /**
     * Without a size the whole list is fetched, so the count query would be a pointless second round trip.
     */
    @Test
    void does_not_count_when_not_paginated() {
        List<String> capturedSql = new ArrayList<>();

        MockDataProvider provider = ctx -> {
            capturedSql.add(ctx.sql());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.DOGS.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetDogListJooqAdapter(dsl).getDogs(filter(null, null));

        assertThat(capturedSql).hasSize(1);
        assertThat(capturedSql.getFirst()).doesNotContain("count(*)").doesNotContain("offset");
    }

    private DogListFilter filter(String owner, String creator) {
        return new DogListFilter(owner, creator, null, null, null);
    }
}
