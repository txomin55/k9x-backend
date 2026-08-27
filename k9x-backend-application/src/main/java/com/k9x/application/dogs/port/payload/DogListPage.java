package com.k9x.application.dogs.port.payload;

import com.k9x.domain.dogs.aggregates.Dog;

import java.util.List;

/**
 * The dogs of the requested page together with how many dogs match the filter across every page.
 */
public record DogListPage(List<Dog> dogs, long total) {
}
