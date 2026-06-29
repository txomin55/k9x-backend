package com.k9x.infrastructure.out.postgres.stages;

import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The adapter delegates to {@link com.k9x.infrastructure.out.postgres.competitions.CompetitionHydrator}, which
 * issues several sequential SELECTs (competitions, stages, events, competitors, exercises, judges, scores).
 * The {@link MockDataProvider} routes by the FROM table, returning one competition and empty results for the
 * rest — enough to assert the adapter hydrates the whole tree with no competition filter (every competition).
 */
class GetStagesJooqAdapterTest {

    private static final Field<String> ORGANIZER_NAME = Tables.ORGANIZERS.NAME.as("organizer_name");

    private static final Field<?>[] COMPETITION_FIELDS = {
            Tables.COMPETITIONS.ID, Tables.COMPETITIONS.NAME, Tables.COMPETITIONS.CREATOR, ORGANIZER_NAME,
            Tables.COMPETITIONS.COUNTRY, Tables.COMPETITIONS.DESCRIPTION, Tables.COMPETITIONS.ADDRESS,
            Tables.COMPETITIONS.COORD_ALT, Tables.COMPETITIONS.COORD_LONG, Tables.COMPETITIONS.LAST_UPDATE,
            Tables.COMPETITIONS.CREATED_AT, Tables.COMPETITIONS.DELETED_AT
    };

    @Test
    void hydrates_every_competition_without_id_filter() {
        AtomicReference<String> competitionsSql = new AtomicReference<>();

        MockDataProvider provider = ctx -> {
            String sql = ctx.sql().toLowerCase();
            DSLContext mockDsl = DSL.using(SQLDialect.POSTGRES);
            if (sql.contains("from \"k9x\".\"competitions\"")) {
                competitionsSql.set(ctx.sql());
                Result<Record> result = mockDsl.newResult(COMPETITION_FIELDS);
                Record r = mockDsl.newRecord(COMPETITION_FIELDS);
                r.set(Tables.COMPETITIONS.ID, "comp-1");
                r.set(Tables.COMPETITIONS.NAME, "Comp A");
                r.set(ORGANIZER_NAME, "Organizer");
                r.set(Tables.COMPETITIONS.LAST_UPDATE, 0L);
                r.set(Tables.COMPETITIONS.CREATED_AT, 0L);
                result.add(r);
                return new MockResult[]{new MockResult(1, result)};
            }
            return new MockResult[]{new MockResult(0, mockDsl.newResult())};
        };

        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        List<CompetitionSnapshot> competitions = new GetStagesJooqAdapter(dsl).getCompetitions();

        assertThat(competitions).hasSize(1);
        assertThat(competitions.getFirst().id()).isEqualTo("comp-1");
        assertThat(competitions.getFirst().stages()).isEmpty();
        // trueCondition() => the WHERE has no real predicate ("where true"): every competition is hydrated.
        assertThat(competitionsSql.get().toLowerCase())
                .contains("from \"k9x\".\"competitions\"")
                .contains("where true");
    }
}
