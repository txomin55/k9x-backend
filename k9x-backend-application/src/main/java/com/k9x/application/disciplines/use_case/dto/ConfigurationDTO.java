package com.k9x.application.disciplines.use_case.dto;

import java.util.List;

public record ConfigurationDTO(String id, String name, List<ExerciseDTO> exercises) {
}
