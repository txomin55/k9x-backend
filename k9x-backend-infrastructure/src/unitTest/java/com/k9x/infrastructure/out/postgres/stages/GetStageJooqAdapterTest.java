package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.domain.aggregates.stages.Stage;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetStageJooqAdapterTest {

    @Test
    void generates_sql_filtered_by_id() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.STAGES.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        new GetStageJooqAdapter(dsl).getStage("stage-123");

        assertThat(capturedSql.get())
                .contains("from \"k9x\".\"stages\"")
                .contains("where \"k9x\".\"stages\".\"id\" = ?");
        assertThat(capturedBindings.get()).containsExactly("stage-123");
    }

    @Test
    void maps_record_to_stage_domain() {
        MockDataProvider provider = _ -> {
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            Result<Record> result = mockDsl.newResult(Tables.STAGES.fields());
            Record record = mockDsl.newRecord(Tables.STAGES.fields());
            record.set(Tables.STAGES.ID, "stage-123");
            record.set(Tables.STAGES.NAME, "Stage 1");
            record.set(Tables.STAGES.COMPETITION_ID, "comp-1");
            record.set(Tables.STAGES.CREATOR, "user-1");
            record.set(Tables.STAGES.DATE_TO, 9999999999999L);
            record.set(Tables.STAGES.LAST_UPDATE, 1000L);
            record.set(Tables.STAGES.CREATED_AT, 2000L);
            record.set(Tables.STAGES.DELETED_AT, null);
            result.add(record);
            return new MockResult[]{new MockResult(1, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Stage stage = new GetStageJooqAdapter(dsl).getStage("stage-123");

        assertThat(stage.id()).isEqualTo("stage-123");
        assertThat(stage.name()).isEqualTo("Stage 1");
        assertThat(stage.competitionId()).isEqualTo("comp-1");
        assertThat(stage.creator()).isEqualTo("user-1");
        assertThat(stage.dateTo()).isEqualTo(9999999999999L);
        assertThat(stage.lastUpdate()).isEqualTo(1000L);
        assertThat(stage.createdAt()).isEqualTo(2000L);
        assertThat(stage.deletedAt()).isNull();
    }

    @Test
    void returns_null_when_stage_not_found() {
        MockDataProvider provider = _ -> {
            Result<Record> result = DSL.using(SQLDialect.POSTGRES).newResult(Tables.STAGES.fields());
            return new MockResult[]{new MockResult(0, result)};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        Stage stage = new GetStageJooqAdapter(dsl).getStage("stage-123");

        assertThat(stage).isNull();
    }
}
