package com.k9x.infrastructure.out.postgres.events.obdx;

import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventDataDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventExerciseDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxEventJudgeDTO;
import com.k9x.application.events.obdx.use_case.port.GetObdxEventDataPersistencePort;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Dogs;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Judges;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.tables.Users;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventCompetitors;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventExercises;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.tables.EventJudges;
import org.jooq.DSLContext;

import java.util.Arrays;
import java.util.List;

public class GetObdxEventDataJooqAdapter implements GetObdxEventDataPersistencePort {

    private final DSLContext dsl;

    public GetObdxEventDataJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public FetchObdxEventDataDTO getEventData(String eventId) {
        return new FetchObdxEventDataDTO(
                fetchCompetitors(eventId),
                fetchExercises(eventId),
                fetchJudges(eventId));
    }

    private List<FetchObdxEventCompetitorDTO> fetchCompetitors(String eventId) {
        EventCompetitors ec = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_COMPETITORS;
        Dogs d = Tables.DOGS;

        return dsl.select(ec.DOG_ID, ec.POSITION, ec.VERIFIED,
                        d.NAME, d.IDENTITY, d.BREED, d.OWNER, d.TEAM, d.COUNTRY)
                .from(ec)
                .join(d).on(d.ID.eq(ec.DOG_ID).and(d.DELETED_AT.isNull()))
                .where(ec.EVENT_ID.eq(eventId))
                .fetch(r -> new FetchObdxEventCompetitorDTO(
                        r.get(ec.DOG_ID),
                        r.get(d.NAME),
                        r.get(d.IDENTITY),
                        r.get(d.BREED),
                        r.get(d.OWNER),
                        r.get(d.TEAM),
                        r.get(d.COUNTRY),
                        r.get(ec.POSITION),
                        r.get(ec.VERIFIED),
                        null
                ));
    }

    private List<FetchObdxEventExerciseDTO> fetchExercises(String eventId) {
        EventExercises ee = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_EXERCISES;

        return dsl.select(ee.EXERCISE_ID, ee.POSITION, ee.TAGS)
                .from(ee)
                .where(ee.EVENT_ID.eq(eventId))
                .orderBy(ee.POSITION)
                .fetch(r -> new FetchObdxEventExerciseDTO(
                        r.get(ee.EXERCISE_ID),
                        r.get(ee.POSITION),
                        r.get(ee.TAGS) == null ? List.of() : Arrays.stream(r.get(ee.TAGS)).toList()
                ));
    }

    private List<FetchObdxEventJudgeDTO> fetchJudges(String eventId) {
        EventJudges ej = com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables.EVENT_JUDGES;
        Judges j = Tables.JUDGES;
        Users u = Tables.USERS;

        return dsl.select(ej.JUDGE_ID, j.NAME, u.EMAIL)
                .from(ej)
                .join(j).on(j.ID.eq(ej.JUDGE_ID).and(j.DELETED_AT.isNull()))
                .leftJoin(u).on(u.ID.eq(ej.COLLECTOR_ID))
                .where(ej.EVENT_ID.eq(eventId))
                .fetch(r -> new FetchObdxEventJudgeDTO(
                        r.get(ej.JUDGE_ID),
                        r.get(j.NAME),
                        r.get(u.EMAIL)
                ));
    }
}
