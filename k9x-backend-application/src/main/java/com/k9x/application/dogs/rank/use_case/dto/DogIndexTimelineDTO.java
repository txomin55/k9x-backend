package com.k9x.application.dogs.rank.use_case.dto;

import java.util.List;

/**
 * Everything the dog's index chart needs. {@code freshnessDegradationFrom} is the instant the whole index starts
 * fading if the dog does not compete again, {@code null} when it has never competed.
 */
public record DogIndexTimelineDTO(List<DogIndexEventDTO> events, List<DogIndexPointDTO> curve,
                                  Long freshnessDegradationFrom) {
}
