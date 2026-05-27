package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GetObdxEventCollectorJooqAdapterTest {

    @Test
    void returns_collector_id_when_present() {
        DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
        MockDataProvider provider = _ -> {
            Result<Record> result = mockDsl.newResult(new org.jooq.Field[]{Tables.EVENT_JUDGES.COLLECTOR_ID});
            Record record = mockDsl.newRecord(new org.jooq.Field[]{Tables.EVENT_JUDGES.COLLECTOR_ID});
            record.set(Tables.EVENT_JUDGES.COLLECTOR_ID, "user@k9x.io");
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);

        String collector = new GetObdxEventCollectorJooqAdapter(dsl).getCollectorId("event-1", "judge-1");

        assertThat(collector).isEqualTo("user@k9x.io");
    }

    @Test
    void returns_null_when_row_missing() {
        DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
        MockDataProvider provider = _ -> {
            Result<Record> result = mockDsl.newResult(new org.jooq.Field[]{Tables.EVENT_JUDGES.COLLECTOR_ID});
            return new MockResult[]{new MockResult(0, result)};
        };
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);

        String collector = new GetObdxEventCollectorJooqAdapter(dsl).getCollectorId("event-1", "judge-1");

        assertThat(collector).isNull();
    }
}
