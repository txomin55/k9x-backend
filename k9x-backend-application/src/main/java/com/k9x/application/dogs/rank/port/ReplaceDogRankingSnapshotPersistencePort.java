package com.k9x.application.dogs.rank.port;

public interface ReplaceDogRankingSnapshotPersistencePort {

    /**
     * Rewrites {@code k9x.snap_dog_ranking} from scratch: one row per active dog with its current index (the
     * latest {@code k9x.snap_dog_index_history} record) and its country, all stamped with {@code computedAt}.
     */
    void replace(long computedAt);
}
