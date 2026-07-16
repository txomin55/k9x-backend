package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.snapshot.port.UpdateObdxCompetitorPositionsPersistencePort;
import com.k9x.application.events.snapshot.port.payload.ObdxCompetitorPosition;
import org.jooq.DSLContext;
import org.jooq.Query;

import java.util.List;

import static com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;

public class UpdateObdxCompetitorPositionsJooqAdapter implements UpdateObdxCompetitorPositionsPersistencePort {

    private final DSLContext dsl;

    public UpdateObdxCompetitorPositionsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updatePositions(String eventId, List<ObdxCompetitorPosition> positions) {
        List<? extends Query> batch = positions.stream()
                .map(p -> dsl.update(EVENT_COMPETITORS)
                        .set(EVENT_COMPETITORS.POSITION, p.position())
                        .where(EVENT_COMPETITORS.EVENT_ID.eq(eventId)
                                .and(EVENT_COMPETITORS.DOG_ID.eq(p.dogId()))))
                .toList();
        if (!batch.isEmpty()) {
            dsl.batch(batch).execute();
        }
    }
}
