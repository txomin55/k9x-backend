package com.k9x.application.dogs.rank.use_case.dto;

/** One event on the dog's index chart: the result itself and the index right after it. */
public record DogIndexEventDTO(FetchDogIndexEventDTO result, int indexAfter) {
}
