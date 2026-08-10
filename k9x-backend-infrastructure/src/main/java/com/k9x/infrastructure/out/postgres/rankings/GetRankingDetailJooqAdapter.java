package com.k9x.infrastructure.out.postgres.rankings;

import com.k9x.application.rankings.port.GetRankingDetailPersistencePort;
import com.k9x.application.rankings.use_case.dto.FetchRankingDTO;
import com.k9x.application.rankings.use_case.dto.FetchRankingEventDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.ArrayList;
import java.util.List;

public class GetRankingDetailJooqAdapter implements GetRankingDetailPersistencePort {

    private final DSLContext dsl;

    public GetRankingDetailJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public FetchRankingDTO getRankingDetail(String id, String creator) {
        // The event_ids array carries no foreign key, so the join both resolves the names and drops ids
        // that no longer point at a live event. The deleted_at filter sits in the ON clause, not the WHERE,
        // so a ranking whose events have all been deleted still comes back — with an empty event list.
        var rows = dsl.select(
                        Tables.RANKINGS.ID,
                        Tables.RANKINGS.NAME,
                        Tables.RANKINGS.GROUP_BY,
                        Tables.RANKINGS.INCLUDE_BY,
                        Tables.RANKINGS.INCLUDED_COUNT,
                        Tables.RANKINGS.INCLUDE_RESERVES,
                        Tables.EVENTS.ID,
                        Tables.EVENTS.NAME)
                .from(Tables.RANKINGS)
                .leftJoin(Tables.EVENTS)
                .on(DSL.condition("{0} = any({1})", Tables.EVENTS.ID, Tables.RANKINGS.EVENT_IDS)
                        .and(Tables.EVENTS.DELETED_AT.isNull()))
                .where(Tables.RANKINGS.ID.eq(id))
                .and(Tables.RANKINGS.CREATOR.eq(creator))
                .fetch();

        if (rows.isEmpty()) {
            return null;
        }

        List<FetchRankingEventDTO> events = new ArrayList<>();
        rows.forEach(row -> {
            if (row.get(Tables.EVENTS.ID) != null) {
                events.add(new FetchRankingEventDTO(
                        row.get(Tables.EVENTS.ID),
                        row.get(Tables.EVENTS.NAME)));
            }
        });

        var first = rows.getFirst();
        return new FetchRankingDTO(
                first.get(Tables.RANKINGS.ID),
                first.get(Tables.RANKINGS.NAME),
                events,
                first.get(Tables.RANKINGS.GROUP_BY),
                first.get(Tables.RANKINGS.INCLUDE_BY),
                first.get(Tables.RANKINGS.INCLUDED_COUNT),
                first.get(Tables.RANKINGS.INCLUDE_RESERVES));
    }
}
