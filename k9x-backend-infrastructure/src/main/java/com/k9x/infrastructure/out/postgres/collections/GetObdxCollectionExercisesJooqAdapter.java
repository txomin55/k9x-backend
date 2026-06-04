package com.k9x.infrastructure.out.postgres.collections;

import com.k9x.application.collections.obdx.port.GetObdxCollectionExercisesPersistencePort;
import com.k9x.application.collections.use_case.dto.FetchCollectionExerciseDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.obdx.Tables;
import org.jooq.DSLContext;

import java.util.List;

public class GetObdxCollectionExercisesJooqAdapter implements GetObdxCollectionExercisesPersistencePort {

    private final DSLContext dsl;

    public GetObdxCollectionExercisesJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<FetchCollectionExerciseDTO> getExercises(String eventId) {
        return dsl.select(Tables.EVENT_EXERCISES.EXERCISE_ID, Tables.EVENT_EXERCISES.POSITION)
                .from(Tables.EVENT_EXERCISES)
                .where(Tables.EVENT_EXERCISES.EVENT_ID.eq(eventId))
                .orderBy(Tables.EVENT_EXERCISES.POSITION)
                .fetch(r -> new FetchCollectionExerciseDTO(
                        r.get(Tables.EVENT_EXERCISES.EXERCISE_ID),
                        r.get(Tables.EVENT_EXERCISES.POSITION)
                ));
    }
}
