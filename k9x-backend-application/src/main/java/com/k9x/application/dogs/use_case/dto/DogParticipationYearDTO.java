package com.k9x.application.dogs.use_case.dto;

import java.util.List;

public record DogParticipationYearDTO(int year, List<DogParticipationDTO> participations) {
}
