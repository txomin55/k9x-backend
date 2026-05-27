package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.port.UpdateObdxScorePersistencePort;
import com.k9x.application.events.obdx.port.payload.UpdateObdxScorePersistencePayload;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
import org.jooq.DSLContext;

public class UpdateObdxScoreJooqAdapter implements UpdateObdxScorePersistencePort {

    private final DSLContext dsl;

    public UpdateObdxScoreJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updateScore(String eventId, UpdateObdxScorePersistencePayload payload) {
        dsl.insertInto(Tables.EVENT_SCORES)
                .set(Tables.EVENT_SCORES.EVENT_ID, eventId)
                .set(Tables.EVENT_SCORES.EXERCISE_ID, payload.exerciseId())
                .set(Tables.EVENT_SCORES.JUDGE_ID, payload.judgeId())
                .set(Tables.EVENT_SCORES.DOG_ID, payload.dogId())
                .set(Tables.EVENT_SCORES.SCORE, payload.score())
                .set(Tables.EVENT_SCORES.CREATED_AT, payload.lastUpdate())
                .set(Tables.EVENT_SCORES.LAST_UPDATE, payload.lastUpdate())
                .onConflict(Tables.EVENT_SCORES.EVENT_ID, Tables.EVENT_SCORES.EXERCISE_ID,
                        Tables.EVENT_SCORES.JUDGE_ID, Tables.EVENT_SCORES.DOG_ID)
                .doUpdate()
                .set(Tables.EVENT_SCORES.SCORE, payload.score())
                .set(Tables.EVENT_SCORES.LAST_UPDATE, payload.lastUpdate())
                .execute();
    }
}
