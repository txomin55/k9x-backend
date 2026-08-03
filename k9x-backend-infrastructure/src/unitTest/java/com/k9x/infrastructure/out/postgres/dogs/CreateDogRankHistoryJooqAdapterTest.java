package com.k9x.infrastructure.out.postgres.dogs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.dogs.rank.port.payload.DogRankHistoryPayload;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CreateDogRankHistoryJooqAdapterTest {

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
        // Inline parameters so the serialized metadata is visible in the captured SQL.
        return DSL.using(new MockConnection(provider), SQLDialect.POSTGRES,
                new Settings().withStatementType(StatementType.STATIC_STATEMENT));
    }

    @Test
    void inserts_each_record_with_json_metadata() {
        List<String> sqls = Collections.synchronizedList(new ArrayList<>());
        DSLContext dsl = capturingDsl(sqls);

        new CreateDogRankHistoryJooqAdapter(dsl, new ObjectMapper()).create(List.of(
                DogRankHistoryPayload.fromEvent("dog-1", "OBDX", 800, 1700000000000L, "evt-1"),
                DogRankHistoryPayload.fromTimeDegradation("dog-2", "OBDX", 799, 1700000000000L, 10)));

        assertThat(sqls).hasSize(2);
        assertThat(sqls.get(0))
                .contains("insert into \"k9x\".\"snap_dog_index_history\"")
                .contains("\"metadata\"").contains("\"applying_timestamp\"")
                .contains("EVENT").contains("evt-1")
                .contains("on conflict").contains("do nothing");
        assertThat(sqls.get(1)).contains("TIME_DEGRADATION").contains("10");
    }

    @Test
    void does_nothing_when_there_are_no_records() {
        List<String> sqls = Collections.synchronizedList(new ArrayList<>());
        DSLContext dsl = capturingDsl(sqls);

        new CreateDogRankHistoryJooqAdapter(dsl, new ObjectMapper()).create(List.of());

        assertThat(sqls).isEmpty();
    }

    @Test
    void event_and_degradation_payloads_carry_their_type() {
        assertThat(DogRankHistoryPayload.fromEvent("d", "OBDX", 1, 1L, "e").metadata())
                .isEqualTo(Map.of("type", "EVENT", "eventId", "e"));
        assertThat(DogRankHistoryPayload.fromTimeDegradation("d", "OBDX", 1, 1L, 12).metadata())
                .isEqualTo(Map.of("type", "TIME_DEGRADATION", "month", "12"));
    }
}
