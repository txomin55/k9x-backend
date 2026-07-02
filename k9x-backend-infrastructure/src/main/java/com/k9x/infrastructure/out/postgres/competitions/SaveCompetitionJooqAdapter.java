package com.k9x.infrastructure.out.postgres.competitions;

import com.k9x.application.competitions.port.SaveCompetitionPersistencePort;
import com.k9x.domain.competitions.aggregates.CompetitionAggregate;
import com.k9x.domain.competitions.commands.*;
import com.k9x.domain.competitions.commands.ObdxCompetitorItem;
import com.k9x.domain.competitions.commands.ObdxExerciseItem;
import com.k9x.domain.competitions.commands.ObdxJudgeItem;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.List;

import static com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.*;

/**
 * Persists a {@link CompetitionAggregate} by replaying its pending changes inside a single
 * transaction, emitting only the SQL affected by each change.
 */
public class SaveCompetitionJooqAdapter implements SaveCompetitionPersistencePort {

    private final DSLContext dsl;

    public SaveCompetitionJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public void save(CompetitionAggregate competition) {
        dsl.transaction(cfg -> {
            DSLContext ctx = DSL.using(cfg);
            for (CompetitionChange change : competition.pendingChanges()) {
                apply(ctx, change);
            }
        });
    }

    private void apply(DSLContext ctx, CompetitionChange change) {
        switch (change) {
            case CompetitionCreated c -> insertCompetition(ctx, c);
            case CompetitionUpdated c -> updateCompetition(ctx, c);
            case CompetitionDeleted c -> softDeleteCompetition(ctx, c);
            case StageCreated c -> insertStage(ctx, c);
            case StageRenamed c -> updateStage(ctx, c);
            case StageDeleted c -> softDeleteStage(ctx, c);
            case EventCreated c -> insertEvent(ctx, c);
            case EventDeleted c -> softDeleteEvent(ctx, c);
            case DogEnrolled c -> insertCompetitor(ctx, c);
            case ObdxEventInfoUpdated c -> updateObdxEventInfo(ctx, c);
            case ScoreUpdated c -> upsertScore(ctx, c);
            case YellowCardRegistered c -> registerYellowCard(ctx, c);
            case CompetitorNotCompetingUpdated c -> updateCompetitorNotCompeting(ctx, c);
            default ->
                    throw new UnsupportedOperationException("Unsupported change type: " + change.getClass().getSimpleName());
        }
    }

    private void insertEvent(DSLContext ctx, EventCreated c) {
        ctx.insertInto(Tables.EVENTS)
                .set(Tables.EVENTS.ID, c.id())
                .set(Tables.EVENTS.NAME, c.name())
                .set(Tables.EVENTS.STAGE_ID, c.stageId())
                .set(Tables.EVENTS.DISCIPLINE, c.discipline())
                .set(Tables.EVENTS.CREATOR, c.creator())
                .set(Tables.EVENTS.CREATED_AT, c.createdAt())
                .set(Tables.EVENTS.LAST_UPDATE, c.createdAt())
                .execute();
    }

    private void softDeleteEvent(DSLContext ctx, EventDeleted c) {
        ctx.update(Tables.EVENTS)
                .set(Tables.EVENTS.DELETED_AT, c.deletedAt())
                .where(Tables.EVENTS.ID.eq(c.id()))
                .execute();
    }

    private void insertCompetitor(DSLContext ctx, DogEnrolled c) {
        ctx.insertInto(EVENT_COMPETITORS)
                .set(EVENT_COMPETITORS.EVENT_ID, c.eventId())
                .set(EVENT_COMPETITORS.DOG_ID, c.dogId())
                .set(EVENT_COMPETITORS.VERIFIED, false)
                .set(EVENT_COMPETITORS.BIH, c.bih())
                .set(EVENT_COMPETITORS.POSITION, c.position())
                .set(EVENT_COMPETITORS.LAST_UPDATE, c.lastUpdate())
                .execute();
    }

    private void upsertScore(DSLContext ctx, ScoreUpdated c) {
        ctx.insertInto(EVENT_SCORES)
                .set(EVENT_SCORES.EVENT_ID, c.eventId())
                .set(EVENT_SCORES.EXERCISE_ID, c.exerciseId())
                .set(EVENT_SCORES.JUDGE_ID, c.judgeId())
                .set(EVENT_SCORES.DOG_ID, c.dogId())
                .set(EVENT_SCORES.SCORE, c.score())
                .set(EVENT_SCORES.CREATED_AT, c.lastUpdate())
                .set(EVENT_SCORES.LAST_UPDATE, c.lastUpdate())
                .onConflict(EVENT_SCORES.EVENT_ID, EVENT_SCORES.EXERCISE_ID, EVENT_SCORES.JUDGE_ID, EVENT_SCORES.DOG_ID)
                .doUpdate()
                .set(EVENT_SCORES.SCORE, c.score())
                .set(EVENT_SCORES.LAST_UPDATE, c.lastUpdate())
                .execute();
    }

    /**
     * A yellow card can be registered before any score exists for that exercise×judge×dog, so this is an
     * upsert: inserts a scoreless row stamped with the card if none exists yet, otherwise just stamps the
     * card (and last_update) onto the existing row without touching its score.
     */
    private void registerYellowCard(DSLContext ctx, YellowCardRegistered c) {
        ctx.insertInto(EVENT_SCORES)
                .set(EVENT_SCORES.EVENT_ID, c.eventId())
                .set(EVENT_SCORES.EXERCISE_ID, c.exerciseId())
                .set(EVENT_SCORES.JUDGE_ID, c.judgeId())
                .set(EVENT_SCORES.DOG_ID, c.dogId())
                .set(EVENT_SCORES.SCORE, (BigDecimal) null)
                .set(EVENT_SCORES.YELLOW_CARD, c.lastUpdate())
                .set(EVENT_SCORES.CREATED_AT, c.lastUpdate())
                .set(EVENT_SCORES.LAST_UPDATE, c.lastUpdate())
                .onConflict(EVENT_SCORES.EVENT_ID, EVENT_SCORES.EXERCISE_ID, EVENT_SCORES.JUDGE_ID, EVENT_SCORES.DOG_ID)
                .doUpdate()
                .set(EVENT_SCORES.YELLOW_CARD, c.lastUpdate())
                .set(EVENT_SCORES.LAST_UPDATE, c.lastUpdate())
                .execute();
    }

    private void updateObdxEventInfo(DSLContext ctx, ObdxEventInfoUpdated c) {
        ctx.update(Tables.EVENTS)
                .set(Tables.EVENTS.NAME, c.name())
                .set(Tables.EVENTS.CONFIGURATION_ID, c.configurationId())
                .set(Tables.EVENTS.SCORE_CALCULATION, c.scoreCalculation().name())
                .set(Tables.EVENTS.ENROLLMENT_DEADLINE, c.enrollmentDeadline())
                .set(Tables.EVENTS.LAST_UPDATE, c.lastUpdate())
                .where(Tables.EVENTS.ID.eq(c.eventId()))
                .execute();

        List<String> newExerciseIds = c.exercises().stream().map(ObdxExerciseItem::exerciseId).toList();
        List<String> newJudgeIds = c.judges().stream().map(ObdxJudgeItem::judgeId).toList();
        List<String> newDogIds = c.competitors().stream().map(ObdxCompetitorItem::dogId).toList();

        ctx.deleteFrom(EVENT_SCORES)
                .where(EVENT_SCORES.EVENT_ID.eq(c.eventId()))
                .and(EVENT_SCORES.EXERCISE_ID.notIn(newExerciseIds)
                        .or(EVENT_SCORES.JUDGE_ID.notIn(newJudgeIds))
                        .or(EVENT_SCORES.DOG_ID.notIn(newDogIds)))
                .execute();

        ctx.deleteFrom(EVENT_COMPETITORS).where(EVENT_COMPETITORS.EVENT_ID.eq(c.eventId())).execute();
        for (ObdxCompetitorItem competitor : c.competitors()) {
            ctx.insertInto(EVENT_COMPETITORS)
                    .set(EVENT_COMPETITORS.EVENT_ID, c.eventId())
                    .set(EVENT_COMPETITORS.DOG_ID, competitor.dogId())
                    .set(EVENT_COMPETITORS.POSITION, competitor.position())
                    .set(EVENT_COMPETITORS.VERIFIED, true)
                    .set(EVENT_COMPETITORS.LAST_UPDATE, c.lastUpdate())
                    .execute();
        }

        ctx.deleteFrom(EVENT_EXERCISES).where(EVENT_EXERCISES.EVENT_ID.eq(c.eventId())).execute();
        for (ObdxExerciseItem exercise : c.exercises()) {
            ctx.insertInto(EVENT_EXERCISES)
                    .set(EVENT_EXERCISES.EVENT_ID, c.eventId())
                    .set(EVENT_EXERCISES.EXERCISE_ID, exercise.exerciseId())
                    .set(EVENT_EXERCISES.POSITION, exercise.position())
                    .set(EVENT_EXERCISES.TAGS, exercise.tags())
                    .set(EVENT_EXERCISES.LAST_UPDATE, c.lastUpdate())
                    .execute();
        }

        ctx.deleteFrom(EVENT_JUDGES).where(EVENT_JUDGES.EVENT_ID.eq(c.eventId())).execute();
        for (ObdxJudgeItem judge : c.judges()) {
            ctx.insertInto(EVENT_JUDGES)
                    .set(EVENT_JUDGES.EVENT_ID, c.eventId())
                    .set(EVENT_JUDGES.JUDGE_ID, judge.judgeId())
                    .set(EVENT_JUDGES.COLLECTOR_ID, judge.collectorId())
                    .set(EVENT_JUDGES.LAST_UPDATE, c.lastUpdate())
                    .execute();
        }

        for (ObdxCompetitorItem competitor : c.competitors()) {
            for (ObdxExerciseItem exercise : c.exercises()) {
                for (ObdxJudgeItem judge : c.judges()) {
                    ctx.insertInto(EVENT_SCORES)
                            .set(EVENT_SCORES.EVENT_ID, c.eventId())
                            .set(EVENT_SCORES.DOG_ID, competitor.dogId())
                            .set(EVENT_SCORES.EXERCISE_ID, exercise.exerciseId())
                            .set(EVENT_SCORES.JUDGE_ID, judge.judgeId())
                            .set(EVENT_SCORES.SCORE, (BigDecimal) null)
                            .set(EVENT_SCORES.CREATED_AT, c.lastUpdate())
                            .set(EVENT_SCORES.LAST_UPDATE, c.lastUpdate())
                            .onConflict(EVENT_SCORES.EVENT_ID, EVENT_SCORES.EXERCISE_ID,
                                    EVENT_SCORES.JUDGE_ID, EVENT_SCORES.DOG_ID)
                            .doNothing()
                            .execute();
                }
            }
        }
    }

    private void updateCompetitorNotCompeting(DSLContext ctx, CompetitorNotCompetingUpdated c) {
        ctx.update(EVENT_COMPETITORS)
                .set(EVENT_COMPETITORS.NOT_COMPETING, c.notCompeting())
                .set(EVENT_COMPETITORS.LAST_UPDATE, c.lastUpdate())
                .where(EVENT_COMPETITORS.EVENT_ID.eq(c.eventId())
                        .and(EVENT_COMPETITORS.DOG_ID.eq(c.dogId())))
                .execute();
    }

    private void insertCompetition(DSLContext ctx, CompetitionCreated c) {
        ctx.insertInto(Tables.COMPETITIONS)
                .set(Tables.COMPETITIONS.ID, c.id())
                .set(Tables.COMPETITIONS.NAME, c.name())
                .set(Tables.COMPETITIONS.COUNTRY, "")
                .set(Tables.COMPETITIONS.CREATOR, c.creator())
                .set(Tables.COMPETITIONS.CREATED_AT, c.createdAt())
                .set(Tables.COMPETITIONS.LAST_UPDATE, c.createdAt())
                .execute();
    }

    private void updateCompetition(DSLContext ctx, CompetitionUpdated c) {
        ctx.update(Tables.COMPETITIONS)
                .set(Tables.COMPETITIONS.NAME, c.name())
                .set(Tables.COMPETITIONS.DESCRIPTION, c.description())
                .set(Tables.COMPETITIONS.COUNTRY, c.country())
                .set(Tables.COMPETITIONS.ADDRESS, c.address())
                .set(Tables.COMPETITIONS.COORD_ALT, c.coordAlt())
                .set(Tables.COMPETITIONS.COORD_LONG, c.coordLong())
                .set(Tables.COMPETITIONS.LAST_UPDATE, c.lastUpdate())
                .where(Tables.COMPETITIONS.ID.eq(c.id()))
                .execute();
    }

    private void softDeleteCompetition(DSLContext ctx, CompetitionDeleted c) {
        ctx.update(Tables.COMPETITIONS)
                .set(Tables.COMPETITIONS.DELETED_AT, c.deletedAt())
                .where(Tables.COMPETITIONS.ID.eq(c.id()))
                .execute();
    }

    private void insertStage(DSLContext ctx, StageCreated c) {
        ctx.insertInto(Tables.STAGES)
                .set(Tables.STAGES.ID, c.id())
                .set(Tables.STAGES.NAME, c.name())
                .set(Tables.STAGES.COMPETITION_ID, c.competitionId())
                .set(Tables.STAGES.DATE_FROM, c.dateFrom())
                .set(Tables.STAGES.DATE_TO, c.dateTo())
                .set(Tables.STAGES.CREATOR, c.creator())
                .set(Tables.STAGES.CREATED_AT, c.createdAt())
                .set(Tables.STAGES.LAST_UPDATE, c.createdAt())
                .execute();
    }

    private void updateStage(DSLContext ctx, StageRenamed c) {
        ctx.update(Tables.STAGES)
                .set(Tables.STAGES.NAME, c.name())
                .set(Tables.STAGES.DATE_FROM, c.dateFrom())
                .set(Tables.STAGES.DATE_TO, c.dateTo())
                .set(Tables.STAGES.LAST_UPDATE, c.lastUpdate())
                .where(Tables.STAGES.ID.eq(c.id()))
                .execute();
    }

    private void softDeleteStage(DSLContext ctx, StageDeleted c) {
        ctx.update(Tables.STAGES)
                .set(Tables.STAGES.DELETED_AT, c.deletedAt())
                .where(Tables.STAGES.ID.eq(c.id()))
                .execute();
    }
}
