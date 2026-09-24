package com.k9x.infrastructure.out.postgres.dogs;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReplaceDogRankingSnapshotJooqAdapterTest {

    private final List<String> sqls = new ArrayList<>();

    private DSLContext capturingDsl() {
        MockDataProvider provider = ctx -> {
            sqls.add(ctx.sql());
            Result<Record> empty = DSL.using(SQLDialect.POSTGRES).newResult();
            return new MockResult[]{new MockResult(0, empty)};
        };
        // Inline parameters so the snapshot instant is visible in the captured SQL.
        return DSL.using(new MockConnection(provider), SQLDialect.POSTGRES,
                new Settings().withStatementType(StatementType.STATIC_STATEMENT));
    }

    @Test
    void empties_the_snapshot_and_refills_it_from_the_latest_history_record_of_each_active_dog() {
        new ReplaceDogRankingSnapshotJooqAdapter(capturingDsl()).replace(1800000000000L);

        assertThat(sqls).hasSize(2);
        assertThat(sqls.get(0))
                .startsWith("delete from \"k9x\".\"snap_dog_ranking\"")
                .doesNotContain("truncate");
        assertThat(sqls.get(1))
                .contains("insert into \"k9x\".\"snap_dog_ranking\"")
                .contains("select distinct on")
                .contains("\"k9x\".\"snap_dog_index_history\"")
                .contains("join \"k9x\".\"dogs\"")
                .contains("\"deleted_at\" is null")
                .contains("\"applying_timestamp\" desc")
                .contains("1800000000000");
    }
}
