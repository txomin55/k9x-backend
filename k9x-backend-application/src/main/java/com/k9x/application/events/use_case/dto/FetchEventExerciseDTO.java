package com.k9x.application.events.use_case.dto;

import java.util.List;

public record FetchEventExerciseDTO(String id, String name, Integer position, List<String> tags) {
}
