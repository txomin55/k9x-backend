package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankEventResultDTO;
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
import java.util.ArrayList;
import java.util.List;

import static com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.SNAP_DOG_RANK;
import static org.assertj.core.api.Assertions.assertThat;

class GetDogRankEventResultsJooqAdapterTest {

    private final List<String> sqls = new ArrayList<>();

    private DSLContext dslReturning(Object[]... rows) {
        MockDataProvider provider = ctx -> {
            sqls.add(ctx.sql());
            Field<?>[] fields = {SNAP_DOG_RANK.DOG_IDENTIFICATION, SNAP_DOG_RANK.DISCIPLINE, SNAP_DOG_RANK.EVENT_ID,
                    SNAP_DOG_RANK.RANK, SNAP_DOG_RANK.TIMESTAMP};
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(fields);
            for (Object[] row : rows) {
                Record record = DSL.using(SQLDialect.POSTGRES).newRecord(fields);
                record.from(row);
                result.add(record);
            }
            return new MockResult[]{new MockResult(rows.length, result)};
        };
        return DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
    }

    @Test
    void fetches_the_raw_per_event_rank_scores_across_disciplines() {
        DSLContext dsl = dslReturning(new Object[]{"dog-1", "OBDX", "evt-1", new BigDecimal("773.14"), 1700000000000L});

        List<FetchDogRankEventResultDTO> results =
                new GetDogRankEventResultsJooqAdapter(dsl).getEventResults();

        assertThat(results).containsExactly(
                new FetchDogRankEventResultDTO("dog-1", "OBDX", "evt-1", new BigDecimal("773.14"), 1700000000000L));
        assertThat(sqls).hasSize(1);
        assertThat(sqls.get(0))
                .contains("\"k9x\".\"snap_dog_rank\"")
                .contains("\"discipline\"")
                .contains("order by");
    }
}
