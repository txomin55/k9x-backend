package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.obdx.port.GetObdxRedCardPersistencePort;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxRedCardDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Judges;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventScores;
import org.jooq.DSLContext;
import org.jooq.Record4;

public class GetObdxRedCardJooqAdapter implements GetObdxRedCardPersistencePort {

    private final DSLContext dsl;

    public GetObdxRedCardJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * A dog can only ever hold one red card in an event, so this reads at most one score row with a
     * stamped red card, carrying the exercise, judge (name resolved from {@code k9x.judges}) and the
     * stamped timestamp. Returns {@code null} when no red card is registered.
     */
    @Override
    public FetchObdxRedCardDTO getRedCard(String eventId, String competitorId) {
        EventScores es = EventScores.EVENT_SCORES;
        Judges j = Judges.JUDGES;

        Record4<String, String, String, Long> r = dsl.select(es.EXERCISE_ID, es.JUDGE_ID, j.NAME, es.RED_CARD)
                .from(es)
                .join(j).on(j.ID.eq(es.JUDGE_ID))
                .where(es.EVENT_ID.eq(eventId))
                .and(es.DOG_ID.eq(competitorId))
                .and(es.RED_CARD.isNotNull())
                .fetchOne();
        if (r == null) {
            return null;
        }
        return new FetchObdxRedCardDTO(r.get(es.EXERCISE_ID), r.get(es.JUDGE_ID), r.get(j.NAME), r.get(es.RED_CARD));
    }
}
