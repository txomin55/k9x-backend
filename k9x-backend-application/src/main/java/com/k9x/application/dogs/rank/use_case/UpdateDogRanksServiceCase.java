package com.k9x.application.dogs.rank.use_case;

import com.k9x.application.dogs.rank.port.GetDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.UpdateDogRanksPersistencePort;
import com.k9x.application.dogs.rank.port.payload.DogRankUpdatePayload;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankDTO;
import com.k9x.application.shared.TransactionalUseCase;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.disciplines.valueobjects.Discipline;
import com.k9x.domain.dogs.rank.DogRankIndex;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recomputes {@code k9x.dogs.rank} for every dog with OBDX rank history as the competitor index of
 * {@code K9X_indice_nivel_spec.md}: the age-weighted mean of the dog's whole {@code k9x.dog_rank} history
 * (level) scaled by how recent its latest result is (freshness) — see {@link DogRankIndex}. Meant to be
 * triggered by a scheduler every 15 days. The history itself is never modified: the index is a pure function
 * of it, recomputed from scratch on each run. Dogs without any history keep a {@code null} rank; a dog that
 * competes again gets a fresh history row from the snapshot cron, so the next run restores freshness to 1.0.
 */
public class UpdateDogRanksServiceCase implements TransactionalUseCase {

    private static final Logger log = System.getLogger(UpdateDogRanksServiceCase.class.getName());

    private final GetDogRankHistoryPersistencePort getDogRankHistoryPersistencePort;
    private final UpdateDogRanksPersistencePort updateDogRanksPersistencePort;

    public UpdateDogRanksServiceCase(GetDogRankHistoryPersistencePort getDogRankHistoryPersistencePort,
                                     UpdateDogRanksPersistencePort updateDogRanksPersistencePort) {
        this.getDogRankHistoryPersistencePort = getDogRankHistoryPersistencePort;
        this.updateDogRanksPersistencePort = updateDogRanksPersistencePort;
    }

    public void updateDogRanks() {
        long now = DateUtils.nowUtcMillis();
        List<FetchDogRankDTO> history = getDogRankHistoryPersistencePort.getDogRankHistory(Discipline.OBDX.name());

        Map<String, List<DogRankIndex.Result>> resultsByDog = new LinkedHashMap<>();
        history.forEach(row -> resultsByDog
                .computeIfAbsent(row.dogId(), dogId -> new ArrayList<>())
                .add(new DogRankIndex.Result(row.rank(), row.timestamp())));

        List<DogRankUpdatePayload> updates = resultsByDog.entrySet().stream()
                .map(dog -> DogRankUpdatePayload.from(dog.getKey(), DogRankIndex.of(dog.getValue(), now)))
                .toList();
        if (!updates.isEmpty()) {
            updateDogRanksPersistencePort.updateRanks(updates);
        }
        log.log(Level.INFO, "Updated rank for {0} dog(s)", updates.size());
    }
}
