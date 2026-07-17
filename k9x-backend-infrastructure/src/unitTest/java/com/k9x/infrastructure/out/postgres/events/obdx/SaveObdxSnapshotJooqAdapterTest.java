package com.k9x.infrastructure.out.postgres.events.obdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.snapshot.port.payload.ObdxCompetitorPosition;
import org.jooq.DSLContext;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaveObdxSnapshotJooqAdapterTest {

    private FetchClassificationDTO classification() {
        return new FetchClassificationDTO("evt-1", "Event", "FINISHED", "stage-1", "Stage A", "WC",
                "obdx", "cfg", "Cfg", 5000L,
                new FetchObdxClassificationDTO(5000L, List.of(), "AVG", List.of()), "A+");
    }

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
    void writes_competitor_position_and_rank_score_plus_snapshot_marker() {
        List<String> sqls = Collections.synchronizedList(new ArrayList<>());
        DSLContext dsl = capturingDsl(sqls);

        new SaveObdxSnapshotJooqAdapter(dsl, new ObjectMapper()).save("evt-1", 1700000000000L, classification(),
                List.of(new ObdxCompetitorPosition("dog-1", (short) 1, new BigDecimal("475.50")),
                        new ObdxCompetitorPosition("dog-2", (short) 3, null)));

        assertThat(sqls).anyMatch(s -> s.contains("update \"obdx\".\"event_competitors\"")
                && s.contains("\"position\"") && s.contains("\"rank_score\""));
        assertThat(sqls).anyMatch(s -> s.contains("insert into \"obdx\".\"event_snapshot\"")
                && s.contains("on conflict") && s.contains("do nothing"));
    }

    @Test
    void writes_only_the_snapshot_marker_when_there_are_no_competitors() {
        List<String> sqls = Collections.synchronizedList(new ArrayList<>());
        DSLContext dsl = capturingDsl(sqls);

        new SaveObdxSnapshotJooqAdapter(dsl, new ObjectMapper()).save("evt-1", 1700000000000L, classification(),
                List.of());

        assertThat(sqls).noneMatch(s -> s.contains("update \"obdx\".\"event_competitors\""));
        assertThat(sqls).anyMatch(s -> s.contains("insert into \"obdx\".\"event_snapshot\""));
    }
}
