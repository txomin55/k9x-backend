package com.k9x.infrastructure.out.postgres.events;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetClassificationEventJooqAdapterTest {

    @Test
    void loads_the_event_by_its_id_and_never_its_competition() {
        List<String> sqls = new ArrayList<>();
        List<List<Object>> bindings = new ArrayList<>();
        MockDataProvider provider = ctx -> {
            sqls.add(ctx.sql().toLowerCase());
            bindings.add(List.of(ctx.bindings()));
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.POSTGRES).newResult())};
        };

        assertThat(new GetClassificationEventJooqAdapter(DSL.using(new MockConnection(provider), SQLDialect.POSTGRES))
                .getEvent("evt-1")).isEmpty();

        assertThat(sqls).hasSize(1);
        assertThat(sqls.getFirst()).contains("\"k9x\".\"events\".\"id\" in").doesNotContain("\"k9x\".\"competitions\"");
        assertThat(bindings.getFirst()).containsExactly("evt-1");
    }
}
