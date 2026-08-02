package com.k9x.infrastructure.out.postgres.dogs;

import com.k9x.application.dogs.rank.port.payload.DogRankUpdatePayload;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDogRanksJooqAdapterTest {

    private DSLContext capturingDsl(List<String> sqls) {
        MockDataProvider provider = ctx -> {
            String[] sql = ctx.batch() ? ctx.batchSQL() : new String[]{ctx.sql()};
            Collections.addAll(sqls, sql);
            Result<Record> empty = DSL.using(SQLDialect.POSTGRES).newResult();
            MockResult[] out = new MockResult[sql.length];
            for (int i = 0; i < out.length; i++) {
                out[i] = new MockResult(1, empty);
            }
            return out;
        };
        return DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
    }

    @Test
    void updates_rank_and_last_update_per_dog() {
        List<String> sqls = Collections.synchronizedList(new ArrayList<>());
        DSLContext dsl = capturingDsl(sqls);

        new UpdateDogRanksJooqAdapter(dsl).updateRanks(List.of(
                new DogRankUpdatePayload("dog-1", 773, 1700000000000L),
                new DogRankUpdatePayload("dog-2", 350, 1700000000000L)));

        assertThat(sqls).hasSize(2);
        assertThat(sqls).allMatch(s -> s.contains("update \"k9x\".\"dogs\"")
                && s.contains("\"rank\"") && s.contains("\"last_update\""));
    }

    @Test
    void does_nothing_when_there_are_no_updates() {
        List<String> sqls = Collections.synchronizedList(new ArrayList<>());
        DSLContext dsl = capturingDsl(sqls);

        new UpdateDogRanksJooqAdapter(dsl).updateRanks(List.of());

        assertThat(sqls).isEmpty();
    }
}
