package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.port.GetClassificationPersistencePort;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationRawRowDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Dogs;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Judges;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventExercises;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventJudges;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventScores;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.math.BigDecimal;
import java.util.List;

public class GetObdxClassificationJooqAdapter implements GetClassificationPersistencePort {

    private final DSLContext dsl;

    public GetObdxClassificationJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchClassificationRawRowDTO> getClassification(String eventId) {
        EventCompetitors ec = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;
        EventExercises ee = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_EXERCISES;
        EventJudges ej = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_JUDGES;
        EventScores es = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_SCORES;
        Dogs d = Tables.DOGS;
        Judges j = Tables.JUDGES;

        Field<String> dogName = d.NAME.as("dog_name");
        Field<String> judgeName = j.NAME.as("judge_name");
        Field<Short> exercisePosition = ee.POSITION.as("exercise_position");
        Field<BigDecimal> score = es.SCORE.as("score");
        Field<Long> scoreLastUpdate = es.LAST_UPDATE.as("score_last_update");

        return dsl.select(
                        ec.DOG_ID, dogName, d.OWNER, d.TEAM, d.COUNTRY,
                        ee.EXERCISE_ID, exercisePosition, ee.TAGS,
                        ej.JUDGE_ID, judgeName,
                        score, scoreLastUpdate)
                .from(ec)
                .join(d).on(d.ID.eq(ec.DOG_ID))
                .join(ee).on(ee.EVENT_ID.eq(ec.EVENT_ID))
                .join(ej).on(ej.EVENT_ID.eq(ec.EVENT_ID))
                .join(j).on(j.ID.eq(ej.JUDGE_ID))
                .leftJoin(es).on(
                        es.EVENT_ID.eq(ec.EVENT_ID)
                                .and(es.DOG_ID.eq(ec.DOG_ID))
                                .and(es.EXERCISE_ID.eq(ee.EXERCISE_ID))
                                .and(es.JUDGE_ID.eq(ej.JUDGE_ID)))
                .where(ec.EVENT_ID.eq(eventId))
                .orderBy(ee.POSITION)
                .fetch(r -> new FetchClassificationRawRowDTO(
                        r.get(ec.DOG_ID), r.get(dogName), r.get(d.OWNER), r.get(d.TEAM), r.get(d.COUNTRY),
                        r.get(ee.EXERCISE_ID), r.get(exercisePosition), r.get(ee.TAGS),
                        r.get(ej.JUDGE_ID), r.get(judgeName),
                        r.get(score), r.get(scoreLastUpdate)));
    }
}
