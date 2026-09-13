package com.k9x.application.dogs.use_case.dto;

import com.k9x.domain.dogs.aggregates.Dog;
import com.k9x.domain.dogs.aggregates.Sex;

/**
 * Everything the public directory tells about a single dog: the whole aggregate minus who owns it, who
 * created the record and when it was created — the three facts that say something about the app's users
 * rather than about the dog.
 *
 * <p>{@code deletedAt} is left out too, for a different reason: the read only ever resolves active dogs, so
 * it would always be {@code null}.
 */
public record PublicDogDetailDTO(String identification, String name, String image, String breed, String origin,
                                 String license, String country, String team, String handler, Sex sex,
                                 Integer withersCm, Boolean threeFciGenerationsConfirmed, long lastUpdate) {

    public static PublicDogDetailDTO from(Dog dog) {
        return new PublicDogDetailDTO(
                dog.identification(),
                dog.name(),
                dog.image(),
                dog.breed(),
                dog.origin(),
                dog.license(),
                dog.country(),
                dog.team(),
                dog.handler(),
                dog.sex(),
                dog.withersCm(),
                dog.threeFciGenerationsConfirmed(),
                dog.lastUpdate());
    }
}
