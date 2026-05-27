package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.port.UpdateObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.payload.UpdateObdxEventPersistencePayload;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

public class UpdateObdxEventJooqAdapter implements UpdateObdxEventPersistencePort {

    private final DSLContext dsl;

    public UpdateObdxEventJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void updateEvent(String id, UpdateObdxEventPersistencePayload payload) {
        dsl.transaction(configuration -> {
            DSLContext ctx = DSL.using(configuration);

            ctx.update(Tables.EVENTS)
                    .set(Tables.EVENTS.NAME, payload.name())
                    .set(Tables.EVENTS.CONFIGURATION_ID, payload.configurationId())
                    .set(Tables.EVENTS.LAST_UPDATE, payload.lastUpdate())
                    .where(Tables.EVENTS.ID.eq(id))
                    .execute();

            ctx.deleteFrom(Tables.EVENT_COMPETITORS)
                    .where(Tables.EVENT_COMPETITORS.EVENT_ID.eq(id))
                    .execute();

            for (UpdateObdxEventPersistencePayload.CompetitorItem competitor : payload.competitors()) {
                ctx.insertInto(Tables.EVENT_COMPETITORS)
                        .set(Tables.EVENT_COMPETITORS.EVENT_ID, id)
                        .set(Tables.EVENT_COMPETITORS.DOG_ID, competitor.dogId())
                        .set(Tables.EVENT_COMPETITORS.POSITION, competitor.position())
                        .set(Tables.EVENT_COMPETITORS.LAST_UPDATE, payload.lastUpdate())
                        .execute();
            }

            ctx.deleteFrom(Tables.EVENT_EXERCISES)
                    .where(Tables.EVENT_EXERCISES.EVENT_ID.eq(id))
                    .execute();

            for (UpdateObdxEventPersistencePayload.ExerciseItem exercise : payload.exercises()) {
                ctx.insertInto(Tables.EVENT_EXERCISES)
                        .set(Tables.EVENT_EXERCISES.EVENT_ID, id)
                        .set(Tables.EVENT_EXERCISES.EXERCISE_ID, exercise.exerciseId())
                        .set(Tables.EVENT_EXERCISES.POSITION, exercise.position())
                        .set(Tables.EVENT_EXERCISES.TAGS, exercise.tags())
                        .set(Tables.EVENT_EXERCISES.LAST_UPDATE, payload.lastUpdate())
                        .execute();
            }

            ctx.deleteFrom(Tables.EVENT_JUDGES)
                    .where(Tables.EVENT_JUDGES.EVENT_ID.eq(id))
                    .execute();

            for (UpdateObdxEventPersistencePayload.JudgeItem judge : payload.judges()) {
                ctx.insertInto(Tables.EVENT_JUDGES)
                        .set(Tables.EVENT_JUDGES.EVENT_ID, id)
                        .set(Tables.EVENT_JUDGES.JUDGE_ID, judge.judgeId())
                        .set(Tables.EVENT_JUDGES.COLLECTOR_ID, judge.collectorId())
                        .set(Tables.EVENT_JUDGES.LAST_UPDATE, payload.lastUpdate())
                        .execute();
            }
        });
    }
}
