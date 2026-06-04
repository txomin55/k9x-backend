package com.k9x.application.events.obdx.use_case.dto;

import java.util.List;

public record FetchObdxEventExerciseDTO(String exerciseId, Short position, List<String> tags) {
}
