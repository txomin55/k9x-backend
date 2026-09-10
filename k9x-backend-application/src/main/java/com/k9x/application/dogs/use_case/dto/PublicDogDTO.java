package com.k9x.application.dogs.use_case.dto;

import com.k9x.domain.dogs.aggregates.Sex;

/**
 * A dog as the public directory shows it: its identification and its presentation fields, no ownership.
 * {@code rank} is already resolved to what the reader sees, so a dog with no index carries
 * {@link com.k9x.domain.dogs.rank.DogRankIndex#NOT_GENERATED} rather than a null the client would have to
 * interpret.
 */
public record PublicDogDTO(String identification, String name, String handler, String country, Sex sex, String breed, String rank) {
}
