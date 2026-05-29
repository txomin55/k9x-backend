package com.k9x.application.collections.port;

import com.k9x.application.collections.use_case.dto.FetchCollectionExerciseDTO;

import java.util.List;

public interface GetObdxCollectionExercisesPersistencePort {
    List<FetchCollectionExerciseDTO> getExercises(String eventId);
}
