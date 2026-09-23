package com.k9x.application.dogs.rank.use_case;

import com.k9x.application.dogs.exceptions.DogNotFoundException;
import com.k9x.application.dogs.port.GetDogPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogIndexEventsPersistencePort;
import com.k9x.application.dogs.rank.use_case.dto.DogIndexEventDTO;
import com.k9x.application.dogs.rank.use_case.dto.DogIndexPointDTO;
import com.k9x.application.dogs.rank.use_case.dto.DogIndexTimelineDTO;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogIndexEventDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.dogs.rank.DogRankIndex;
import com.k9x.domain.dogs.rank.DogRankIndexTimeline;

import java.util.Comparator;
import java.util.List;

/**
 * The public chart of a dog's index over its career: each result it earned, the index after it, and the curve in
 * between, computed with the same {@link DogRankIndex} formula as the history cron so the chart ends on the same
 * figure the directory shows. The curve is computed on read rather than taken from
 * {@code k9x.snap_dog_index_history}: that history only records events and whole months of inactivity, not the
 * level erosion between two events. Like the public dog detail it asserts nothing about the caller, and a dog
 * that is not active answers "not found".
 */
public class GetDogIndexTimelineServiceCase {

    private static final Comparator<FetchDogIndexEventDTO> OLDEST_FIRST =
            Comparator.comparingLong(FetchDogIndexEventDTO::appliesAt).thenComparing(FetchDogIndexEventDTO::eventId);

    private final GetDogPersistencePort getDogPersistencePort;
    private final GetDogIndexEventsPersistencePort getDogIndexEventsPersistencePort;

    public GetDogIndexTimelineServiceCase(GetDogPersistencePort getDogPersistencePort,
                                          GetDogIndexEventsPersistencePort getDogIndexEventsPersistencePort) {
        this.getDogPersistencePort = getDogPersistencePort;
        this.getDogIndexEventsPersistencePort = getDogIndexEventsPersistencePort;
    }

    public DogIndexTimelineDTO getIndexTimeline(String identification) {
        if (getDogPersistencePort.getDog(identification) == null) {
            throw new DogNotFoundException();
        }

        List<FetchDogIndexEventDTO> results = getDogIndexEventsPersistencePort.getIndexEvents(identification)
                .stream().sorted(OLDEST_FIRST).toList();
        List<DogRankIndex.Result> history = results.stream()
                .map(result -> new DogRankIndex.Result(result.rankScore(), result.appliesAt()))
                .toList();

        // The index after an event counts every result up to that instant, including others on the same day.
        List<DogIndexEventDTO> events = results.stream()
                .map(result -> new DogIndexEventDTO(
                        result,
                        DogRankIndex.of(history.stream()
                                .filter(earlier -> earlier.timestamp() <= result.appliesAt())
                                .toList(), result.appliesAt())))
                .toList();

        List<DogIndexPointDTO> curve = DogRankIndexTimeline.of(history, DateUtils.nowUtcMillis()).stream()
                .map(point -> new DogIndexPointDTO(point.timestamp(), point.index()))
                .toList();

        Long freshnessDegradationFrom = results.isEmpty() ? null
                : DogRankIndex.freshnessDegradationFrom(results.get(results.size() - 1).appliesAt());

        return new DogIndexTimelineDTO(events, curve, freshnessDegradationFrom);
    }
}
