package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.use_case.dto.FetchLatestDogRankHistoryDTO;
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

import java.util.ArrayList;
import java.util.List;

import static com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.SNAP_DOG_INDEX_HISTORY;
import static org.assertj.core.api.Assertions.assertThat;

class GetLatestDogRankHistoryJooqAdapterTest {

    private final List<String> sqls = new ArrayList<>();

    private DSLContext dslReturning(Object[]... rows) {
        MockDataProvider provider = ctx -> {
            sqls.add(ctx.sql());
            Field<?>[] fields = {SNAP_DOG_INDEX_HISTORY.DOG_ID, SNAP_DOG_INDEX_HISTORY.DISCIPLINE, SNAP_DOG_INDEX_HISTORY.RANK,
                    SNAP_DOG_INDEX_HISTORY.APPLYING_TIMESTAMP};
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
    void fetches_the_latest_history_record_per_dog() {
        DSLContext dsl = dslReturning(new Object[]{"dog-1", "OBDX", 760, 1700000000000L});

        List<FetchLatestDogRankHistoryDTO> latest = new GetLatestDogRankHistoryJooqAdapter(dsl).getLatestHistory();

        assertThat(latest).containsExactly(new FetchLatestDogRankHistoryDTO("dog-1", "OBDX", 760, 1700000000000L));
        assertThat(sqls).hasSize(1);
        assertThat(sqls.get(0))
                .contains("distinct on")
                .contains("\"k9x\".\"snap_dog_index_history\"")
                .contains("\"applying_timestamp\"")
                .contains("desc");
    }
}
