package com.k9x.application.dogs.use_case.dto;

import com.k9x.domain.dogs.aggregates.Sex;

/**
 * A public directory row as persistence reads it. The {@code rank} is the dog's latest recorded K9X index and
 * is {@code null} when the history holds none for it yet; turning that absence into an answer the reader can
 * show is the service case's job.
 */
public record FetchPublicDogDTO(String identification, String name, String handler, String country, Sex sex, String breed, Integer rank) {
}
