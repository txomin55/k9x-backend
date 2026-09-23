package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.use_case.dto.FetchDogIndexEventDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.SNAP_DOG_RANK;
import static com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.SNAP_EVENT_COMPETITORS_RESULTS;
import static org.assertj.core.api.Assertions.assertThat;

class GetDogIndexEventsJooqAdapterTest {

    private static final Field<Boolean> RESTRICTED =
            DSL.field(DSL.name("latest_extraction", "restricted"), Boolean.class);

    private static final Field<?>[] SELECTED = {
            Tables.EVENTS.ID, Tables.EVENTS.NAME, Tables.STAGES.ID, SNAP_DOG_RANK.DISCIPLINE,
            Tables.COMPETITIONS.COUNTRY, SNAP_DOG_RANK.APPLYING_TIMESTAMP, SNAP_DOG_RANK.RANK,
            SNAP_EVENT_COMPETITORS_RESULTS.POSITION, SNAP_EVENT_COMPETITORS_RESULTS.TOTAL_SCORE, RESTRICTED};

    /** Every rank row the cron counts, deleted or not, so the chart ends on the dog's current index. */
    @Test
    void reads_every_rank_row_of_the_dog_without_a_soft_delete_filter() {
        AtomicReference<String> sql = new AtomicReference<>();

        adapter(sql, empty()).getIndexEvents("DOG-1");

        assertThat(sql.get())
                .contains("from \"k9x\".\"snap_dog_rank\"")
                .contains("\"k9x\".\"snap_dog_rank\".\"dog_identification\" = ?")
                .doesNotContain("deleted_at");
    }

    @Test
    void left_joins_the_placing_and_the_latest_extraction() {
        AtomicReference<String> sql = new AtomicReference<>();

        adapter(sql, empty()).getIndexEvents("DOG-1");

        assertThat(sql.get())
                .contains("left outer join \"obdx\".\"snap_event_competitors_results\"")
                .contains("\"obdx\".\"snap_event_competitors_results\".\"dog_identification\" = "
                        + "\"k9x\".\"snap_dog_rank\".\"dog_identification\"")
                .contains("distinct on (\"k9x\".\"extraction_metadata\".\"competition_id\")")
                .contains("\"k9x\".\"extraction_metadata\".\"extraction_timestamp\" desc");
    }

    @Test
    void maps_every_column_and_treats_a_missing_extraction_as_not_restricted() {
        DSLContext ctx = DSL.using(SQLDialect.POSTGRES);
        Result<Record> result = ctx.newResult(SELECTED);
        result.add(row(ctx, "E1", "Grade 1", "S1", "OBDX", "ES", 1_000L, new BigDecimal("750.00"), (short) 2,
                new BigDecimal("251.50"), null));
        result.add(row(ctx, "E2", "Grade 2", "S2", "OBDX", null, 2_000L, new BigDecimal("600.00"), null, null,
                true));

        List<FetchDogIndexEventDTO> events = adapter(new AtomicReference<>(), result).getIndexEvents("DOG-1");

        assertThat(events).containsExactly(
                new FetchDogIndexEventDTO("E1", "Grade 1", "S1", "OBDX", "ES", 1_000L, new BigDecimal("750.00"),
                        (short) 2, new BigDecimal("251.50"), false),
                new FetchDogIndexEventDTO("E2", "Grade 2", "S2", "OBDX", null, 2_000L, new BigDecimal("600.00"),
                        null, null, true));
    }

    private static GetDogIndexEventsJooqAdapter adapter(AtomicReference<String> sql, Result<Record> result) {
        MockDataProvider provider = context -> {
            sql.set(context.sql());
            return new MockResult[]{new MockResult(result.size(), result)};
        };
        return new GetDogIndexEventsJooqAdapter(DSL.using(new MockConnection(provider), SQLDialect.POSTGRES));
    }

    private static Result<Record> empty() {
        return DSL.using(SQLDialect.POSTGRES).newResult(SELECTED);
    }

    @SuppressWarnings("unchecked")
    private static Record row(DSLContext ctx, Object... values) {
        Record record = ctx.newRecord(SELECTED);
        for (int i = 0; i < values.length; i++) {
            record.set((Field<Object>) SELECTED[i], values[i]);
        }
        return record;
    }
}
