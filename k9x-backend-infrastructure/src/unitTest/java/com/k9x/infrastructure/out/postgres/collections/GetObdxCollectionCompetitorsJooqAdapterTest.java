package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.use_case.dto.FetchCollectionCompetitorDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Dogs;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GetObdxCollectionCompetitorsJooqAdapterTest {

    private static final EventCompetitors EC = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;
    private static final Dogs D = Tables.DOGS;

    private static final Field<?>[] SELECT_FIELDS = {
            EC.DOG_ID, EC.POSITION, EC.VERIFIED, EC.NOT_COMPETING, EC.BIH, EC.RESERVE,
            D.NAME, D.IDENTITY, D.BREED, D.OWNER, D.HANDLER, D.TEAM, D.COUNTRY
    };

    private static final Field<?>[] JOIN_FIELDS = Stream.of(
            Arrays.stream(EC.fields()),
            Arrays.stream(D.fields())
    ).flatMap(s -> s).toArray(Field[]::new);

    @Test
    void generates_sql_with_join_to_dogs_filtered_by_event_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(JOIN_FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetObdxCollectionCompetitorsJooqAdapter(dsl).getCompetitors("event-1");

        assertThat(capturedSql.get())
                .contains("from \"obdx\".\"event_competitors\"")
                .contains("join \"k9x\".\"dogs\"")
                .contains("\"obdx\".\"event_competitors\".\"event_id\" = ?");
        assertThat(capturedBindings.get()).containsExactly("event-1");
    }

    @Test
    void maps_record_to_dto() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(SELECT_FIELDS);
            Record record = mockDsl.newRecord(SELECT_FIELDS);
            record.set(EC.DOG_ID, "dog-1");
            record.set(EC.POSITION, (short) 1);
            record.set(EC.VERIFIED, true);
            record.set(EC.NOT_COMPETING, true);
            record.set(EC.BIH, true);
            record.set(EC.RESERVE, true);
            record.set(D.NAME, "Rex");
            record.set(D.IDENTITY, "ID-001");
            record.set(D.BREED, "Border Collie");
            record.set(D.OWNER, "owner@test.com");
            record.set(D.HANDLER, "Rex Handler");
            record.set(D.TEAM, "Team A");
            record.set(D.COUNTRY, "ES");
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCollectionCompetitorDTO> competitors = new GetObdxCollectionCompetitorsJooqAdapter(dsl).getCompetitors("event-1");

        assertThat(competitors).hasSize(1);
        FetchCollectionCompetitorDTO comp = competitors.getFirst();
        assertThat(comp.dogId()).isEqualTo("dog-1");
        assertThat(comp.dogName()).isEqualTo("Rex");
        assertThat(comp.dogIdentity()).isEqualTo("ID-001");
        assertThat(comp.breed()).isEqualTo("Border Collie");
        assertThat(comp.owner()).isEqualTo("owner@test.com");
        assertThat(comp.handler()).isEqualTo("Rex Handler");
        assertThat(comp.team()).isEqualTo("Team A");
        assertThat(comp.country()).isEqualTo("ES");
        assertThat(comp.position()).isEqualTo((short) 1);
        assertThat(comp.verified()).isTrue();
        assertThat(comp.notCompeting()).isTrue();
        assertThat(comp.bih()).isTrue();
        assertThat(comp.reserve()).isTrue();
        assertThat(comp.status()).isNull();
    }

    @Test
    void returns_empty_list_when_no_results() {
        MockDataProvider provider = _ -> new MockResult[]{
                new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(JOIN_FIELDS))
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCollectionCompetitorDTO> competitors = new GetObdxCollectionCompetitorsJooqAdapter(dsl).getCompetitors("event-1");

        assertThat(competitors).isEmpty();
    }
}
