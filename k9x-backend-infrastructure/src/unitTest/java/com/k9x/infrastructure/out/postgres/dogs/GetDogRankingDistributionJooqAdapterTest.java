package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankingDistributionDTO;
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
import java.util.Map;

import static com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables.SNAP_DOG_RANKING;
import static org.assertj.core.api.Assertions.assertThat;

class GetDogRankingDistributionJooqAdapterTest {

    private final List<String> sqls = new ArrayList<>();

    private static Result<Record> result(Field<?>[] fields, Object[]... rows) {
        Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(fields);
        for (Object[] row : rows) {
            Record record = DSL.using(SQLDialect.POSTGRES).newRecord(fields);
            record.from(row);
            result.add(record);
        }
        return result;
    }

    /** Answers the per-index count with {@code counts} and the snapshot instant with {@code computedAt}. */
    private DSLContext dslReturning(Long computedAt, Object[]... counts) {
        MockDataProvider provider = ctx -> {
            sqls.add(ctx.sql());
            if (ctx.sql().contains("group by")) {
                Field<?>[] fields = {SNAP_DOG_RANKING.RANK, DSL.count()};
                return new MockResult[]{new MockResult(counts.length, result(fields, counts))};
            }
            Field<?>[] fields = {DSL.max(SNAP_DOG_RANKING.COMPUTED_AT)};
            return new MockResult[]{new MockResult(1, result(fields, new Object[]{computedAt}))};
        };
        return DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
    }

    @Test
    void counts_the_snapshot_dogs_per_index_from_the_threshold() {
        DSLContext dsl = dslReturning(1800000000000L, new Object[]{953, 1}, new Object[]{410, 3});

        FetchDogRankingDistributionDTO distribution =
                new GetDogRankingDistributionJooqAdapter(dsl).getDistribution(null, 100);

        assertThat(distribution.computedAt()).isEqualTo(1800000000000L);
        assertThat(distribution.dogsByIndex()).isEqualTo(Map.of(953, 1, 410, 3));
        assertThat(sqls).hasSize(2);
        assertThat(sqls.get(0))
                .contains("\"k9x\".\"snap_dog_ranking\"")
                .contains("\"rank\" >= ?")
                .contains("group by")
                .doesNotContain("\"country\" = ?");
    }

    @Test
    void filters_by_the_country_stamped_in_the_snapshot() {
        DSLContext dsl = dslReturning(1800000000000L);

        new GetDogRankingDistributionJooqAdapter(dsl).getDistribution("ES", 100);

        assertThat(sqls.get(0))
                .contains("\"k9x\".\"snap_dog_ranking\".\"country\" = ?")
                .doesNotContain("\"dogs\"");
    }

    @Test
    void has_no_instant_while_the_snapshot_is_empty() {
        DSLContext dsl = dslReturning(null);

        FetchDogRankingDistributionDTO distribution =
                new GetDogRankingDistributionJooqAdapter(dsl).getDistribution(null, 100);

        assertThat(distribution.computedAt()).isNull();
        assertThat(distribution.dogsByIndex()).isEmpty();
    }
}
