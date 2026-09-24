package com.k9x.infrastructure.out.postgres.events;

import com.k9x.application.events.use_case.dto.FetchEventClassificationHeaderDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
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

import static org.assertj.core.api.Assertions.assertThat;

class GetEventClassificationHeaderJooqAdapterTest {

    private static final Field<?>[] FIELDS = {
            Tables.EVENTS.ID, Tables.EVENTS.NAME, Tables.EVENTS.DISCIPLINE,
            com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_INFO.CONFIGURATION_ID,
            Tables.EVENTS.DELETED_AT, Tables.EVENTS.RANK_SCORE, EventProjectionFields.HAS_ANY_SCORE,
            Tables.STAGES.ID, Tables.STAGES.NAME, Tables.STAGES.DATE_TO, Tables.COMPETITIONS.ID, Tables.COMPETITIONS.NAME
    };

    private final List<String> sqls = new ArrayList<>();

    private DSLContext dsl(boolean exists) {
        MockDataProvider provider = ctx -> {
            String sql = ctx.sql().toLowerCase();
            sqls.add(sql);
            DSLContext mock = DSL.using(SQLDialect.POSTGRES);
            if (sql.startsWith("select \"k9x\".\"events\".\"id\"")) {
                Result<Record> result = mock.newResult(FIELDS);
                if (exists) {
                    Record r = mock.newRecord(FIELDS);
                    r.set(Tables.EVENTS.ID, "evt-1");
                    r.set(Tables.EVENTS.NAME, "Open");
                    r.set(Tables.EVENTS.DISCIPLINE, "obdx");
                    r.set(EventProjectionFields.HAS_ANY_SCORE, true);
                    r.set(Tables.STAGES.ID, "s-1");
                    r.set(Tables.STAGES.NAME, "Stage 1");
                    r.set(Tables.STAGES.DATE_TO, 2000L);
                    r.set(Tables.COMPETITIONS.ID, "comp-1");
                    r.set(Tables.COMPETITIONS.NAME, "Comp");
                    result.add(r);
                }
                return new MockResult[]{new MockResult(result.size(), result)};
            }
            return new MockResult[]{new MockResult(0, mock.newResult())};
        };
        return DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
    }

    @Test
    void reads_the_event_its_stage_and_competition_in_one_row_without_loading_scores() {
        Optional<FetchEventClassificationHeaderDTO> header = new GetEventClassificationHeaderJooqAdapter(dsl(true)).getHeader("evt-1");

        assertThat(header).contains(new FetchEventClassificationHeaderDTO("evt-1", "Open", "obdx", null, null, null,
                true, "s-1", "Stage 1", 2000L, "Comp", null));
        assertThat(sqls.getFirst())
                .contains("\"k9x\".\"events\".\"id\" = ?")
                .contains("join \"k9x\".\"stages\"")
                .contains("join \"k9x\".\"competitions\"")
                .contains("exists");
        // Header + latest extraction, nothing else: no sibling events, no score rows.
        assertThat(sqls).hasSize(2);
        assertThat(sqls.get(1)).contains("\"k9x\".\"extraction_metadata\"").contains("fetch next ? rows only");
    }

    @Test
    void returns_empty_when_the_event_does_not_exist() {
        assertThat(new GetEventClassificationHeaderJooqAdapter(dsl(false)).getHeader("evt-1")).isEmpty();
        assertThat(sqls).hasSize(1);
    }
}
