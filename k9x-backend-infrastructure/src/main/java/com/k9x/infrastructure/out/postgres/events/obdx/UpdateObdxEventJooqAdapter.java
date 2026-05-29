package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.port.UpdateObdxEventPersistencePort;
import com.k9x.application.events.obdx.port.payload.UpdateObdxEventPersistencePayload;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.List;

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
                    .set(Tables.EVENTS.SCORE_CALCULATION, payload.scoreCalculation().name())
                    .set(Tables.EVENTS.LAST_UPDATE, payload.lastUpdate())
                    .where(Tables.EVENTS.ID.eq(id))
                    .execute();

            List<String> newExerciseIds = payload.exercises().stream()
                    .map(UpdateObdxEventPersistencePayload.ExerciseItem::exerciseId).toList();
            List<String> newJudgeIds = payload.judges().stream()
                    .map(UpdateObdxEventPersistencePayload.JudgeItem::judgeId).toList();
            List<String> newDogIds = payload.competitors().stream()
                    .map(UpdateObdxEventPersistencePayload.CompetitorItem::dogId).toList();

            ctx.deleteFrom(Tables.EVENT_SCORES)
                    .where(Tables.EVENT_SCORES.EVENT_ID.eq(id))
                    .and(Tables.EVENT_SCORES.EXERCISE_ID.notIn(newExerciseIds)
                            .or(Tables.EVENT_SCORES.JUDGE_ID.notIn(newJudgeIds))
                            .or(Tables.EVENT_SCORES.DOG_ID.notIn(newDogIds)))
                    .execute();

            ctx.deleteFrom(Tables.EVENT_COMPETITORS)
                    .where(Tables.EVENT_COMPETITORS.EVENT_ID.eq(id))
                    .execute();

            for (UpdateObdxEventPersistencePayload.CompetitorItem competitor : payload.competitors()) {
                ctx.insertInto(Tables.EVENT_COMPETITORS)
                        .set(Tables.EVENT_COMPETITORS.EVENT_ID, id)
                        .set(Tables.EVENT_COMPETITORS.DOG_ID, competitor.dogId())
                        .set(Tables.EVENT_COMPETITORS.POSITION, competitor.position())
                        .set(Tables.EVENT_COMPETITORS.VERIFIED, true)
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

            for (UpdateObdxEventPersistencePayload.CompetitorItem competitor : payload.competitors()) {
                for (UpdateObdxEventPersistencePayload.ExerciseItem exercise : payload.exercises()) {
                    for (UpdateObdxEventPersistencePayload.JudgeItem judge : payload.judges()) {
                        ctx.insertInto(Tables.EVENT_SCORES)
                                .set(Tables.EVENT_SCORES.EVENT_ID, id)
                                .set(Tables.EVENT_SCORES.DOG_ID, competitor.dogId())
                                .set(Tables.EVENT_SCORES.EXERCISE_ID, exercise.exerciseId())
                                .set(Tables.EVENT_SCORES.JUDGE_ID, judge.judgeId())
                                .set(Tables.EVENT_SCORES.SCORE, (BigDecimal) null)
                                .set(Tables.EVENT_SCORES.CREATED_AT, payload.lastUpdate())
                                .set(Tables.EVENT_SCORES.LAST_UPDATE, payload.lastUpdate())
                                .onConflict(Tables.EVENT_SCORES.EVENT_ID, Tables.EVENT_SCORES.EXERCISE_ID,
                                        Tables.EVENT_SCORES.JUDGE_ID, Tables.EVENT_SCORES.DOG_ID)
                                .doNothing()
                                .execute();
                    }
                }
            }
        });
    }
}
