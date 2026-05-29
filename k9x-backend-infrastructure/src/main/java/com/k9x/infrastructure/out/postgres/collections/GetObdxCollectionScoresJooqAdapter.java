package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.port.GetObdxCollectionScoresPersistencePort;
import com.k9x.application.collections.use_case.dto.FetchCollectionScoreDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
import org.jooq.DSLContext;

import java.util.List;

public class GetObdxCollectionScoresJooqAdapter implements GetObdxCollectionScoresPersistencePort {

    private final DSLContext dsl;

    public GetObdxCollectionScoresJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchCollectionScoreDTO> getScores(String eventId) {
        return dsl.select(Tables.EVENT_SCORES.DOG_ID, Tables.EVENT_SCORES.EXERCISE_ID,
                        Tables.EVENT_SCORES.JUDGE_ID, Tables.EVENT_SCORES.SCORE)
                .from(Tables.EVENT_SCORES)
                .where(Tables.EVENT_SCORES.EVENT_ID.eq(eventId))
                .fetch(r -> new FetchCollectionScoreDTO(
                        r.get(Tables.EVENT_SCORES.DOG_ID),
                        r.get(Tables.EVENT_SCORES.EXERCISE_ID),
                        r.get(Tables.EVENT_SCORES.JUDGE_ID),
                        r.get(Tables.EVENT_SCORES.SCORE)
                ));
    }
}
