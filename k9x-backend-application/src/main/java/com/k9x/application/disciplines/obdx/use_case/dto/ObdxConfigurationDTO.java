package com.k9x.application.disciplines.obdx.use_case.dto;

import java.util.List;

public record ObdxConfigurationDTO(String id, String name, List<ExerciseDTO> exercises) {
}
