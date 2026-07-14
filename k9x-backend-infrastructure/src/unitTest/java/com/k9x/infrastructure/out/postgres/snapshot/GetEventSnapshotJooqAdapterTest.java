package com.k9x.infrastructure.out.postgres.snapshot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventSnapshot;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GetEventSnapshotJooqAdapterTest {

    private static final EventSnapshot ES = EventSnapshot.EVENT_SNAPSHOT;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void deserializes_stored_snapshot() throws Exception {
        FetchClassificationDTO stored = new FetchClassificationDTO("evt-1", "Event", "FINISHED", "stage-1",
                "Stage A", "WC", "obdx", "cfg", "Cfg", 5000L,
                new FetchObdxClassificationDTO(5000L, List.of(), "AVG", List.of()));
        String json = MAPPER.writeValueAsString(stored);

        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(new org.jooq.Field<?>[]{ES.SNAPSHOT});
            Record record = mockDsl.newRecord(new org.jooq.Field<?>[]{ES.SNAPSHOT});
            record.set(ES.SNAPSHOT, JSON.valueOf(json));
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Optional<FetchClassificationDTO> result =
                new GetEventSnapshotJooqAdapter(dsl, MAPPER).getSnapshot("evt-1");

        assertThat(result).isPresent();
        assertThat(result.get().eventId()).isEqualTo("evt-1");
        assertThat(result.get().stageName()).isEqualTo("Stage A");
        assertThat(result.get().obdx().scoreCalculation()).isEqualTo("AVG");
    }

    @Test
    void returns_empty_when_no_row() {
        MockDataProvider provider = _ -> new MockResult[]{
                new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(ES.SNAPSHOT))
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        assertThat(new GetEventSnapshotJooqAdapter(dsl, MAPPER).getSnapshot("evt-1")).isEmpty();
    }
}
