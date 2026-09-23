package com.k9x.application.dogs.use_case;

import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.application.dogs.port.GetDogParticipationsPersistencePort;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.use_case.dto.DogParticipationDTO;
import com.k9x.application.dogs.use_case.dto.DogParticipationYearDTO;
import com.k9x.domain.shared.UtcDates;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The public record of every event a dog was entered in, grouped by the UTC year its stage started: newest year
 * first, and newest event first within a year. Like the public dog detail it asserts nothing about the caller,
 * and a dog that is not active answers "not found" rather than an empty history.
 */
public class GetDogParticipationsServiceCase {

    private static final Comparator<DogParticipationDTO> NEWEST_FIRST =
            Comparator.comparingLong(DogParticipationDTO::stageDateFrom).reversed()
                    .thenComparing(DogParticipationDTO::eventName, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(DogParticipationDTO::eventId);

    private final GetDogPersistencePort getDogPersistencePort;
    private final GetDogParticipationsPersistencePort getDogParticipationsPersistencePort;

    public GetDogParticipationsServiceCase(GetDogPersistencePort getDogPersistencePort,
                                           GetDogParticipationsPersistencePort getDogParticipationsPersistencePort) {
        this.getDogPersistencePort = getDogPersistencePort;
        this.getDogParticipationsPersistencePort = getDogParticipationsPersistencePort;
    }

    public List<DogParticipationYearDTO> getParticipations(String identification) {
        if (getDogPersistencePort.getDog(identification) == null) {
            throw new DogNotFoundException();
        }

        Map<Integer, List<DogParticipationDTO>> byYear = new TreeMap<>(Comparator.reverseOrder());
        getDogParticipationsPersistencePort.getParticipations(identification)
                .forEach(participation -> byYear
                        .computeIfAbsent(UtcDates.utcDay(participation.stageDateFrom()).getYear(),
                                _ -> new ArrayList<>())
                        .add(participation));

        return byYear.entrySet().stream()
                .map(year -> new DogParticipationYearDTO(year.getKey(),
                        year.getValue().stream().sorted(NEWEST_FIRST).toList()))
                .toList();
    }
}
