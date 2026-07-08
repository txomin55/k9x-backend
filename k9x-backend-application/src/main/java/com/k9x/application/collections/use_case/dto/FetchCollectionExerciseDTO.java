package com.k9x.application.collections.use_case.dto;

import java.util.List;

public record FetchCollectionExerciseDTO(String exerciseId, Short position, List<String> judges) {}
