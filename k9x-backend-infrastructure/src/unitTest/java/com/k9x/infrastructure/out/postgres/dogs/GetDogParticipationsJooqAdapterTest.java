package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.use_case.dto.DogParticipationDTO;
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

import static com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.SNAP_EVENT_COMPETITORS_RESULTS;
import static org.assertj.core.api.Assertions.assertThat;

class GetDogParticipationsJooqAdapterTest {

    private static final Field<Boolean> RESTRICTED =
            DSL.field(DSL.name("latest_extraction", "restricted"), Boolean.class);

    private static final Field<?>[] SELECTED = {
            Tables.EVENTS.ID, Tables.EVENTS.NAME, Tables.STAGES.ID, Tables.STAGES.DATE_FROM,
            Tables.COMPETITIONS.COUNTRY, SNAP_EVENT_COMPETITORS_RESULTS.POSITION,
            SNAP_EVENT_COMPETITORS_RESULTS.TOTAL_SCORE, SNAP_EVENT_COMPETITORS_RESULTS.RANK_SCORE, RESTRICTED};

    @Test
    void reads_only_the_dog_entries_in_active_events_stages_and_competitions() {
        AtomicReference<String> sql = new AtomicReference<>();

        adapter(sql, empty()).getParticipations("DOG-1");

        assertThat(sql.get())
                .contains("\"obdx\".\"event_competitors\".\"dog_identification\" = ?")
                .contains("\"k9x\".\"events\".\"deleted_at\" is null")
                .contains("\"k9x\".\"stages\".\"deleted_at\" is null")
                .contains("\"k9x\".\"competitions\".\"deleted_at\" is null");
    }

    /** An event whose snapshot has not run yet has no result row: it must still be listed, without results. */
    @Test
    void left_joins_the_snapshot_results_and_the_latest_extraction() {
        AtomicReference<String> sql = new AtomicReference<>();

        adapter(sql, empty()).getParticipations("DOG-1");

        assertThat(sql.get())
                .contains("left outer join \"obdx\".\"snap_event_competitors_results\"")
                .contains("\"obdx\".\"snap_event_competitors_results\".\"dog_identification\" = "
                        + "\"obdx\".\"event_competitors\".\"dog_identification\"")
                .contains("distinct on (\"k9x\".\"extraction_metadata\".\"competition_id\")")
                .contains("\"k9x\".\"extraction_metadata\".\"extraction_timestamp\" desc");
    }

    @Test
    void maps_every_column_and_treats_a_missing_extraction_as_not_restricted() {
        DSLContext ctx = DSL.using(SQLDialect.POSTGRES);
        Result<Record> result = ctx.newResult(SELECTED);
        result.add(row(ctx, "E1", "Grade 1", "S1", 1_000L, "ES", (short) 2, new BigDecimal("251.50"),
                new BigDecimal("80.00"), null));
        result.add(row(ctx, "E2", "Grade 2", "S2", 2_000L, null, null, null, null, true));

        List<DogParticipationDTO> participations = adapter(new AtomicReference<>(), result).getParticipations("DOG-1");

        assertThat(participations).containsExactly(
                new DogParticipationDTO("E1", "Grade 1", "S1", 1_000L, "ES", (short) 2,
                        new BigDecimal("251.50"), new BigDecimal("80.00"), false),
                new DogParticipationDTO("E2", "Grade 2", "S2", 2_000L, null, null, null, null, true));
    }

    private static GetDogParticipationsJooqAdapter adapter(AtomicReference<String> sql, Result<Record> result) {
        MockDataProvider provider = context -> {
            sql.set(context.sql());
            return new MockResult[]{new MockResult(result.size(), result)};
        };
        return new GetDogParticipationsJooqAdapter(DSL.using(new MockConnection(provider), SQLDialect.POSTGRES));
    }

    private static Result<Record> empty() {
        return DSL.using(SQLDialect.POSTGRES).newResult(SELECTED);
    }

    private static Record row(DSLContext ctx, Object... values) {
        Record record = ctx.newRecord(SELECTED);
        for (int i = 0; i < values.length; i++) {
            record.set((Field<Object>) SELECTED[i], values[i]);
        }
        return record;
    }
}
