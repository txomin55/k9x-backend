package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankingEntryDTO;
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
import java.util.Optional;

import static com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.DOGS;
import static com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.SNAP_DOG_RANKING;
import static org.assertj.core.api.Assertions.assertThat;

class GetDogRankingEntryJooqAdapterTest {

    private final List<String> sqls = new ArrayList<>();

    private DSLContext dslReturning(Object[]... rows) {
        MockDataProvider provider = ctx -> {
            sqls.add(ctx.sql());
            Field<?>[] fields = {SNAP_DOG_RANKING.DOG_IDENTIFICATION, DOGS.NAME, SNAP_DOG_RANKING.RANK,
                    SNAP_DOG_RANKING.COUNTRY};
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
    void reads_the_dog_snapshot_row_with_its_name() {
        DSLContext dsl = dslReturning(new Object[]{"dog-1", "Rex", 953, "ES"});

        Optional<FetchDogRankingEntryDTO> entry = new GetDogRankingEntryJooqAdapter(dsl).getEntry("dog-1");

        assertThat(entry).contains(new FetchDogRankingEntryDTO("dog-1", "Rex", 953, "ES"));
        assertThat(sqls.get(0))
                .contains("\"k9x\".\"snap_dog_ranking\"")
                .contains("join \"k9x\".\"dogs\"")
                .contains("\"deleted_at\" is null");
    }

    @Test
    void is_empty_when_the_dog_is_not_in_the_snapshot() {
        assertThat(new GetDogRankingEntryJooqAdapter(dslReturning()).getEntry("dog-1")).isEmpty();
    }
}
