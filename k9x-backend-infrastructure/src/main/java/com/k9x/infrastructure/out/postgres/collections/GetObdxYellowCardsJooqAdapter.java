package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.obdx.port.GetObdxYellowCardsPersistencePort;
import com.k9x.application.collections.obdx.use_case.dto.FetchObdxYellowCardDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Judges;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventScores;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.List;

public class GetObdxYellowCardsJooqAdapter implements GetObdxYellowCardsPersistencePort {

    private final DSLContext dsl;

    public GetObdxYellowCardsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Reads the score rows of the competitor in the event and turns each stamped yellow card into its own
     * item, carrying the exercise, judge (name resolved from {@code k9x.judges}) and the stamped timestamp.
     * Rows without a yellow card are skipped.
     */
    @Override
    public List<FetchObdxYellowCardDTO> getYellowCards(String eventId, String competitorId) {
        EventScores es = EventScores.EVENT_SCORES;
        Judges j = Judges.JUDGES;

        List<FetchObdxYellowCardDTO> result = new ArrayList<>();
        dsl.select(es.EXERCISE_ID, es.JUDGE_ID, j.NAME, es.YELLOW_CARD)
                .from(es)
                .join(j).on(j.ID.eq(es.JUDGE_ID))
                .where(es.EVENT_ID.eq(eventId))
                .and(es.DOG_ID.eq(competitorId))
                .forEach(r -> {
                    Long yellowCard = r.get(es.YELLOW_CARD);
                    if (yellowCard != null) {
                        result.add(new FetchObdxYellowCardDTO(r.get(es.EXERCISE_ID), r.get(es.JUDGE_ID),
                                r.get(j.NAME), yellowCard));
                    }
                });
        return result;
    }
}
