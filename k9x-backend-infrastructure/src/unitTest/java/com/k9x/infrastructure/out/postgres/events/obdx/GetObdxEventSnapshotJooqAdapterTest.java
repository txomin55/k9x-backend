package com.k9x.infrastructure.out.postgres.events.obdx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.SnapEventClassification;
import org.jooq.DSLContext;
import org.jooq.Field;
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

class GetObdxEventSnapshotJooqAdapterTest {

    private static final SnapEventClassification ES = SnapEventClassification.SNAP_EVENT_CLASSIFICATION;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void deserializes_stored_snapshot() throws Exception {
        FetchObdxClassificationDTO stored = new FetchObdxClassificationDTO(5000L, List.of(), "AVG", List.of());
        String json = MAPPER.writeValueAsString(stored);

        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(new Field<?>[]{ES.SNAPSHOT});
            Record record = mockDsl.newRecord(new Field<?>[]{ES.SNAPSHOT});
            record.set(ES.SNAPSHOT, JSON.valueOf(json));
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Optional<FetchObdxClassificationDTO> result =
                new GetObdxEventSnapshotJooqAdapter(dsl, MAPPER).getSnapshot("evt-1");

        assertThat(result).isPresent();
        assertThat(result.get().scoresLastUpdate()).isEqualTo(5000L);
        assertThat(result.get().scoreCalculation()).isEqualTo("AVG");
    }

    @Test
    void returns_empty_when_no_row() {
        MockDataProvider provider = _ -> new MockResult[]{
                new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult(new Field<?>[]{ES.SNAPSHOT}))
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        assertThat(new GetObdxEventSnapshotJooqAdapter(dsl, MAPPER).getSnapshot("evt-1")).isEmpty();
    }
}
