package com.k9x.application.dogs.rank.use_case;

import com.k9x.application.dogs.rank.port.CreateDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogRankEventResultsPersistencePort;
import com.k9x.application.dogs.rank.port.GetLatestDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.payload.DogRankHistoryPayload;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankEventResultDTO;
import com.k9x.application.dogs.rank.use_case.dto.FetchLatestDogRankHistoryDTO;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.dogs.rank.DogRankIndex;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Appends each dog's rank timeline to {@code k9x.snap_dog_index_history}: the competitor index of
 * {@code K9X_indice_nivel_spec.md} ({@link DogRankIndex}, level × freshness over the dog's snapshotted event
 * results), recorded only when something actually changes it. The index is a <b>single figure per dog</b>
 * computed over its whole history regardless of discipline — a dog competing in several disciplines still
 * carries one timeline, and every result feeds it on the shared 0-1000 scale. Meant to be triggered by a
 * scheduler; each run appends at most what happened since the dog's latest record:
 *
 * <ul>
 *   <li><b>New event result</b> ({@code k9x.snap_dog_rank} row newer than the dog's latest record) → one
 *       record per new event, its {@code applying_timestamp} being the instant the result applies to (the
 *       event's stage end) and the index computed as of that moment ({@code type=EVENT} + the event id in the
 *       metadata). The {@code timestamp} column always carries the persistence instant instead.</li>
 *   <li><b>Inactivity degradation</b> — no new event, but the dog's inactivity has crossed a new whole month
 *       beyond the {@value DogRankIndex#FRESHNESS_PLATEAU_MONTHS_THRESHOLD}-month freshness plateau → one
 *       record with the freshly degraded index ({@code type=TIME_DEGRADATION} + the crossed month), measured
 *       against the dog's last event in any discipline. Once the freshness floor month
 *       ({@value DogRankIndex#FRESHNESS_FLOOR_MONTHS_THRESHOLD}) has been recorded nothing degrades further,
 *       so no more records are appended.</li>
 * </ul>
 *
 * The history is append-only and never rewritten; a quiet run appends nothing.
 */
public class GenerateDogRankHistoryServiceCase implements TransactionalUseCase {

    private static final Logger log = System.getLogger(GenerateDogRankHistoryServiceCase.class.getName());

    private final GetDogRankEventResultsPersistencePort getDogRankEventResultsPersistencePort;
    private final GetLatestDogRankHistoryPersistencePort getLatestDogRankHistoryPersistencePort;
    private final CreateDogRankHistoryPersistencePort createDogRankHistoryPersistencePort;

    public GenerateDogRankHistoryServiceCase(
            GetDogRankEventResultsPersistencePort getDogRankEventResultsPersistencePort,
            GetLatestDogRankHistoryPersistencePort getLatestDogRankHistoryPersistencePort,
            CreateDogRankHistoryPersistencePort createDogRankHistoryPersistencePort) {
        this.getDogRankEventResultsPersistencePort = getDogRankEventResultsPersistencePort;
        this.getLatestDogRankHistoryPersistencePort = getLatestDogRankHistoryPersistencePort;
        this.createDogRankHistoryPersistencePort = createDogRankHistoryPersistencePort;
    }

    public void generateDogRankHistory() {
        long now = DateUtils.nowUtcMillis();

        Map<String, List<FetchDogRankEventResultDTO>> resultsByDog = new LinkedHashMap<>();
        getDogRankEventResultsPersistencePort.getEventResults().forEach(result -> resultsByDog
                .computeIfAbsent(result.dogIdentification(), dog -> new ArrayList<>())
                .add(result));

        Map<String, FetchLatestDogRankHistoryDTO> latestByDog =
                getLatestDogRankHistoryPersistencePort.getLatestHistory().stream()
                        .collect(Collectors.toMap(FetchLatestDogRankHistoryDTO::dogIdentification, latest -> latest));

        List<DogRankHistoryPayload> records = new ArrayList<>();
        resultsByDog.forEach((dog, results) -> records.addAll(recordsFor(results, latestByDog.get(dog), now)));

        if (!records.isEmpty()) {
            createDogRankHistoryPersistencePort.create(records);
        }
        log.log(Level.INFO, "Appended {0} dog index history record(s)", records.size());
    }

    private List<DogRankHistoryPayload> recordsFor(List<FetchDogRankEventResultDTO> results,
                                                   FetchLatestDogRankHistoryDTO latest, long now) {
        String dogIdentification = results.get(0).dogIdentification();
        long latestRecordedAt = latest == null ? Long.MIN_VALUE : latest.applyingTimestamp();

        // One record per event result not yet reflected in the history, each with the index as of that event
        // (its own freshness plateau: 1.0). Replays the timeline in order, so a first run over an existing dog
        // rebuilds its whole event history.
        List<DogRankHistoryPayload> records = new ArrayList<>();
        List<DogRankIndex.Result> accumulated = new ArrayList<>();
        for (FetchDogRankEventResultDTO result : results) {
            accumulated.add(new DogRankIndex.Result(result.rank(), result.applyingTimestamp()));
            if (result.applyingTimestamp() > latestRecordedAt) {
                int rank = DogRankIndex.of(accumulated, result.applyingTimestamp());
                records.add(DogRankHistoryPayload.fromEvent(dogIdentification, rank, result.applyingTimestamp(),
                        result.eventId()));
            }
        }
        if (!records.isEmpty()) {
            return records;
        }

        // No new event: degrade only when the dog's inactivity crosses a whole month beyond the freshness
        // plateau that the history has not recorded yet — freshness is the curve that keeps moving an inactive
        // dog's index. The recorded month is derived from the latest record's applying timestamp (an EVENT
        // record applies at the event itself -> month 0), and the floor month is recorded at most once.
        long lastEventAt = results.get(results.size() - 1).applyingTimestamp();
        int monthsInactive = Math.min(DogRankIndex.wholeMonthsBetween(lastEventAt, now),
                DogRankIndex.FRESHNESS_FLOOR_MONTHS_THRESHOLD);
        int monthsRecorded = DogRankIndex.wholeMonthsBetween(lastEventAt, latestRecordedAt);
        if (monthsInactive >= DogRankIndex.FRESHNESS_PLATEAU_MONTHS_THRESHOLD && monthsInactive > monthsRecorded) {
            int rank = DogRankIndex.of(accumulated, now);
            return List.of(DogRankHistoryPayload.fromTimeDegradation(dogIdentification, rank, now, monthsInactive));
        }
        return List.of();
    }
}
