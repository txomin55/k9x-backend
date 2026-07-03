package com.k9x.application.dogs.use_case.command;

import com.k9x.domain.dogs.aggregates.Sex;

public record UpdateDogCommand(String name, String image, String breed, String identity, String owner, String handler,
                               String team, String country, Sex sex, Integer withersCm) {
}
