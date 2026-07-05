package com.k9x.application.dogs.use_case.dto;

import com.k9x.domain.dogs.aggregates.Sex;

public record DogDTO(String id, String name, String image, Boolean owned, String country, String team,
                     String owner, String handler, String identity, String breed, Sex sex, Integer withersCm,
                     Boolean threeFciGenerationsConfirmed) {

}
