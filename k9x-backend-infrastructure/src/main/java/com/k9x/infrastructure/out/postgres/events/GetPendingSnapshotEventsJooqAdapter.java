package com.k9x.infrastructure.out.postgres.events;

import com.k9x.application.events.snapshot.port.GetPendingSnapshotEventsPersistencePort;
import com.k9x.application.events.snapshot.use_case.dto.PendingSnapshotEventDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Events;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Stages;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventSnapshot;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

public class GetPendingSnapshotEventsJooqAdapter implements GetPendingSnapshotEventsPersistencePort {

    private final DSLContext dsl;

    public GetPendingSnapshotEventsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<PendingSnapshotEventDTO> getFinishedEventsWithoutSnapshot(long startOfTodayUtcMillis) {
        Events e = Tables.EVENTS;
        Stages s = Tables.STAGES;
        EventSnapshot es = EventSnapshot.EVENT_SNAPSHOT;

        return dsl.select(e.ID, e.DISCIPLINE)
                .from(e)
                .join(s).on(s.ID.eq(e.STAGE_ID).and(s.DELETED_AT.isNull()))
                .where(e.DELETED_AT.isNull())
                .and(s.DATE_TO.lt(startOfTodayUtcMillis))
                .andNotExists(DSL.selectOne().from(es).where(es.EVENT_ID.eq(e.ID)))
                .fetch(r -> new PendingSnapshotEventDTO(r.get(e.ID), r.get(e.DISCIPLINE)));
    }
}
