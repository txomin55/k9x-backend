package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.use_case.dto.FetchCollectionDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Events;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventJudges;
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

class GetCollectionListJooqAdapterTest {

    private static final Events E = Tables.EVENTS;
    private static final EventJudges EJ = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_JUDGES;

    private static final Field<?>[] JOIN_FIELDS = Stream.of(
            Arrays.stream(E.fields()),
            Arrays.stream(Tables.STAGES.fields()),
            Arrays.stream(Tables.COMPETITIONS.fields()),
            Arrays.stream(EJ.fields()),
            Arrays.stream(Tables.JUDGES.fields()),
            Arrays.stream(Tables.USERS.fields())
    ).flatMap(s -> s).toArray(Field[]::new);

    @Test
    void generates_sql_with_joins_filtered_by_collector_email_and_active_stage() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(JOIN_FIELDS);
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetCollectionListJooqAdapter(dsl).getCollections("collector@test.com", 1000L);

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"events\"")
                .contains("join \"k9x\".\"stages\"")
                .contains("join \"k9x\".\"competitions\"")
                .contains("join \"obdx\".\"event_judges\"")
                .contains("join \"k9x\".\"judges\"")
                .contains("join \"k9x\".\"users\"")
                .contains("\"k9x\".\"users\".\"email\" = ?")
                .contains("\"k9x\".\"stages\".\"date_from\" <= ?")
                .contains("\"k9x\".\"stages\".\"date_to\" >= ?")
                .contains("\"k9x\".\"events\".\"deleted_at\" is null");
        assertThat(capturedBindings.get()).containsExactly("collector@test.com", 1000L, 1000L);
    }

    @Test
    void maps_record_to_collection_dto() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(JOIN_FIELDS);
            Record record = mockDsl.newRecord(JOIN_FIELDS);
            record.set(E.ID, "event-1");
            record.set(E.NAME, "Event A");
            record.set(E.DISCIPLINE, "obdx");
            record.set(Tables.STAGES.ID, "stage-1");
            record.set(Tables.STAGES.NAME, "Stage A");
            record.set(Tables.COMPETITIONS.ID, "comp-1");
            record.set(Tables.COMPETITIONS.NAME, "Competition A");
            record.set(Tables.JUDGES.ID, "judge-1");
            record.set(Tables.JUDGES.NAME, "Judge One");
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCollectionDTO> collections = new GetCollectionListJooqAdapter(dsl).getCollections("collector@test.com", 1000L);

        assertThat(collections).hasSize(1);
        FetchCollectionDTO collection = collections.getFirst();
        assertThat(collection.eventId()).isEqualTo("event-1");
        assertThat(collection.eventName()).isEqualTo("Event A");
        assertThat(collection.stageName()).isEqualTo("Stage A");
        assertThat(collection.competitionName()).isEqualTo("Competition A");
        assertThat(collection.discipline()).isEqualTo("obdx");
        assertThat(collection.judges()).hasSize(1);
        assertThat(collection.judges().getFirst().id()).isEqualTo("judge-1");
        assertThat(collection.judges().getFirst().name()).isEqualTo("Judge One");
    }

    @Test
    void deduplicates_event_with_multiple_judges() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(JOIN_FIELDS);

            Record r1 = mockDsl.newRecord(JOIN_FIELDS);
            r1.set(E.ID, "event-1");
            r1.set(E.NAME, "Event A");
            r1.set(Tables.STAGES.ID, "stage-1");
            r1.set(Tables.STAGES.NAME, "Stage A");
            r1.set(Tables.COMPETITIONS.ID, "comp-1");
            r1.set(Tables.COMPETITIONS.NAME, "Competition A");
            r1.set(Tables.JUDGES.ID, "judge-1");
            r1.set(Tables.JUDGES.NAME, "Judge One");

            Record r2 = mockDsl.newRecord(JOIN_FIELDS);
            r2.set(E.ID, "event-1");
            r2.set(E.NAME, "Event A");
            r2.set(Tables.STAGES.ID, "stage-1");
            r2.set(Tables.STAGES.NAME, "Stage A");
            r2.set(Tables.COMPETITIONS.ID, "comp-1");
            r2.set(Tables.COMPETITIONS.NAME, "Competition A");
            r2.set(Tables.JUDGES.ID, "judge-2");
            r2.set(Tables.JUDGES.NAME, "Judge Two");

            result.add(r1);
            result.add(r2);
            return new MockResult[]{new MockResult(2, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCollectionDTO> collections = new GetCollectionListJooqAdapter(dsl).getCollections("collector@test.com", 1000L);

        assertThat(collections).hasSize(1);
        assertThat(collections.getFirst().judges()).hasSize(2);
    }

    @Test
    void returns_empty_list_when_no_results() {
        MockDataProvider provider = _ -> new MockResult[]{
                new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(JOIN_FIELDS))
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<FetchCollectionDTO> collections = new GetCollectionListJooqAdapter(dsl).getCollections("collector@test.com", 1000L);

        assertThat(collections).isEmpty();
    }
}
