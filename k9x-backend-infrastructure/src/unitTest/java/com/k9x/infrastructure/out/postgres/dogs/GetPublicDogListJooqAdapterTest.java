package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.port.payload.PublicDogListFilter;
import com.k9x.application.dogs.port.payload.PublicDogListPage;
import com.k9x.application.dogs.use_case.dto.FetchPublicDogDTO;
import com.k9x.domain.dogs.aggregates.Sex;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Field;
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

class GetPublicDogListJooqAdapterTest {

    private static final Field<Integer> RANK = Tables.SNAP_DOG_INDEX_HISTORY.RANK;

    private static final Field<?>[] SELECTED = {
            Tables.DOGS.IDENTIFICATION, Tables.DOGS.NAME, Tables.DOGS.HANDLER, Tables.DOGS.COUNTRY,
            Tables.DOGS.SEX, Tables.DOGS.BREED, RANK};

    @Test
    void selects_only_the_public_fields_plus_the_index() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        DSLContext dsl = DSL.using(new MockConnection(capturing(capturedSql, null)), SQLDialect.POSTGRES);
        new GetPublicDogListJooqAdapter(dsl).getDogs(filter());

        assertThat(capturedSql.get())
                .contains("\"k9x\".\"dogs\".\"identification\"")
                .contains("\"k9x\".\"dogs\".\"name\"")
                .contains("\"k9x\".\"dogs\".\"handler\"")
                .contains("\"k9x\".\"dogs\".\"country\"")
                .contains("\"k9x\".\"dogs\".\"sex\"")
                .contains("\"k9x\".\"dogs\".\"breed\"")
                .contains("\"latest_index\".\"rank\"")
                .doesNotContain("\"k9x\".\"dogs\".\"owner\"")
                .doesNotContain("\"k9x\".\"dogs\".\"creator\"");
    }

    /**
     * A dog that has never competed has no history row, so the index must be joined rather than filtered on,
     * or the whole directory would shrink to the ranked dogs.
     */
    @Test
    void left_joins_the_latest_index_so_dogs_without_one_are_still_listed() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        DSLContext dsl = DSL.using(new MockConnection(capturing(capturedSql, null)), SQLDialect.POSTGRES);
        new GetPublicDogListJooqAdapter(dsl).getDogs(filter());

        assertThat(capturedSql.get())
                .contains("left outer join")
                .contains("distinct on (\"k9x\".\"snap_dog_index_history\".\"dog_identification\")")
                .contains("order by \"k9x\".\"snap_dog_index_history\".\"dog_identification\", "
                        + "\"k9x\".\"snap_dog_index_history\".\"applying_timestamp\" desc")
                .contains("\"latest_index\".\"dog_identification\" = \"k9x\".\"dogs\".\"identification\"");
    }

    @Test
    void lists_only_the_active_dogs() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        DSLContext dsl = DSL.using(new MockConnection(capturing(capturedSql, null)), SQLDialect.POSTGRES);
        new GetPublicDogListJooqAdapter(dsl).getDogs(filter());

        assertThat(capturedSql.get()).contains("\"k9x\".\"dogs\".\"deleted_at\" is null");
    }

    @Test
    void generates_case_insensitive_name_search() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        DSLContext dsl = DSL.using(new MockConnection(capturing(capturedSql, capturedBindings)), SQLDialect.POSTGRES);
        new GetPublicDogListJooqAdapter(dsl).getDogs(new PublicDogListFilter("re", null, null, null, null));

        assertThat(capturedSql.get()).contains("lower(\"k9x\".\"dogs\".\"name\") like");
        assertThat(capturedBindings.get()).contains("re");
    }

    @Test
    void generates_case_insensitive_handler_search() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        DSLContext dsl = DSL.using(new MockConnection(capturing(capturedSql, capturedBindings)), SQLDialect.POSTGRES);
        new GetPublicDogListJooqAdapter(dsl).getDogs(new PublicDogListFilter(null, "an", null, null, null));

        assertThat(capturedSql.get()).contains("lower(\"k9x\".\"dogs\".\"handler\") like");
        assertThat(capturedBindings.get()).contains("an");
    }

    /**
     * Unlike the search box of the secured list, the public filters narrow the list down together.
     */
    @Test
    void requires_every_given_filter_to_match() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        DSLContext dsl = DSL.using(new MockConnection(capturing(capturedSql, null)), SQLDialect.POSTGRES);
        new GetPublicDogListJooqAdapter(dsl).getDogs(new PublicDogListFilter("re", "an", "ES", null, null));

        assertThat(capturedSql.get())
                .contains("lower(\"k9x\".\"dogs\".\"name\") like")
                .contains("lower(\"k9x\".\"dogs\".\"handler\") like")
                .contains("\"k9x\".\"dogs\".\"country\" = ?")
                .doesNotContain(" or ");
    }

    @Test
    void filters_by_country_when_one_is_given() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        DSLContext dsl = DSL.using(new MockConnection(capturing(capturedSql, capturedBindings)), SQLDialect.POSTGRES);
        new GetPublicDogListJooqAdapter(dsl).getDogs(new PublicDogListFilter(null, null, "ES", null, null));

        assertThat(capturedSql.get()).contains("\"k9x\".\"dogs\".\"country\" = ?");
        assertThat(capturedBindings.get()).containsExactly("ES");
    }

    @Test
    void orders_by_index_first_with_the_unranked_dogs_last() {
        AtomicReference<String> capturedSql = new AtomicReference<>();

        DSLContext dsl = DSL.using(new MockConnection(capturing(capturedSql, null)), SQLDialect.POSTGRES);
        new GetPublicDogListJooqAdapter(dsl).getDogs(filter());

        assertThat(capturedSql.get())
                .contains("order by \"latest_index\".\"rank\" desc nulls last, "
                        + "\"k9x\".\"dogs\".\"name\" asc, \"k9x\".\"dogs\".\"identification\" asc");
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
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(SELECTED))};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        PublicDogListPage page =
                new GetPublicDogListJooqAdapter(dsl).getDogs(new PublicDogListFilter(null, null, null, 40, 20));

        assertThat(capturedSql).anyMatch(sql -> sql.contains("count(*)"));
        assertThat(capturedSql).anyMatch(sql -> sql.contains("offset ?") && sql.contains("fetch next ? rows only"));
        // The total is the whole match count, not the size of the returned page.
        assertThat(page.total()).isEqualTo(137);
        assertThat(page.dogs()).isEmpty();
    }

    /**
     * Every filter is on the dogs table, so the count query has no reason to pay for the index join.
     */
    @Test
    void counts_without_joining_the_index() {
        List<String> capturedSql = new ArrayList<>();

        MockDataProvider provider = ctx -> {
            capturedSql.add(ctx.sql());
            if (ctx.sql().contains("count(*)")) {
                Result<Record1<Integer>> count = DSL.using(SQLDialect.POSTGRES).newResult(DSL.count());
                Record1<Integer> record = DSL.using(SQLDialect.POSTGRES).newRecord(DSL.count());
                record.value1(0);
                count.add(record);
                return new MockResult[]{new MockResult(1, count)};
            }
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(SELECTED))};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetPublicDogListJooqAdapter(dsl).getDogs(new PublicDogListFilter(null, null, null, 0, 20));

        assertThat(capturedSql).filteredOn(sql -> sql.contains("count(*)"))
                .allMatch(sql -> !sql.contains("latest_index"));
    }

    /**
     * Without a size the whole list is fetched, so the count query would be a pointless second round trip.
     */
    @Test
    void does_not_count_when_not_paginated() {
        List<String> capturedSql = new ArrayList<>();

        MockDataProvider provider = ctx -> {
            capturedSql.add(ctx.sql());
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(SELECTED))};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetPublicDogListJooqAdapter(dsl).getDogs(filter());

        assertThat(capturedSql).hasSize(1);
        assertThat(capturedSql.getFirst()).doesNotContain("count(*)").doesNotContain("offset");
    }

    @Test
    void maps_records_to_the_public_read_model() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(SELECTED);
            Record record = mockDsl.newRecord(SELECTED);
            record.set(Tables.DOGS.IDENTIFICATION, "981098106001010");
            record.set(Tables.DOGS.NAME, "Rex");
            record.set(Tables.DOGS.HANDLER, "Ana");
            record.set(Tables.DOGS.COUNTRY, "ES");
            record.set(Tables.DOGS.SEX, "MALE");
            record.set(Tables.DOGS.BREED, "BORDER_COLLIE");
            record.set(RANK, 742);
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        PublicDogListPage page = new GetPublicDogListJooqAdapter(dsl).getDogs(filter());

        assertThat(page.dogs()).hasSize(1);
        assertThat(page.total()).isEqualTo(1);
        FetchPublicDogDTO dog = page.dogs().getFirst();
        assertThat(dog.identification()).isEqualTo("981098106001010");
        assertThat(dog.name()).isEqualTo("Rex");
        assertThat(dog.handler()).isEqualTo("Ana");
        assertThat(dog.country()).isEqualTo("ES");
        assertThat(dog.sex()).isEqualTo(Sex.MALE);
        assertThat(dog.breed()).isEqualTo("BORDER_COLLIE");
        assertThat(dog.rank()).isEqualTo(742);
    }

    /**
     * The absent index travels as a null all the way to the service case, which is where it becomes an answer.
     */
    @Test
    void maps_a_missing_index_to_a_null_rank() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(SELECTED);
            Record record = mockDsl.newRecord(SELECTED);
            record.set(Tables.DOGS.NAME, "Rex");
            record.set(RANK, null);
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        PublicDogListPage page = new GetPublicDogListJooqAdapter(dsl).getDogs(filter());

        assertThat(page.dogs().getFirst().rank()).isNull();
        assertThat(page.dogs().getFirst().sex()).isNull();
    }

    private MockDataProvider capturing(AtomicReference<String> sql, AtomicReference<Object[]> bindings) {
        return ctx -> {
            sql.set(ctx.sql());
            if (bindings != null) {
                bindings.set(ctx.bindings());
            }
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(SELECTED))};
        };
    }

    private PublicDogListFilter filter() {
        return new PublicDogListFilter(null, null, null, null, null);
    }
}
