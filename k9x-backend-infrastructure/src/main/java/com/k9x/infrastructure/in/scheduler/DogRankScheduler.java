package com.k9x.infrastructure.in.scheduler;

import com.k9x.application.dogs.rank.use_case.UpdateDogRanksServiceCase;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Inbound adapter that triggers the periodic refresh of {@code k9x.dogs.rank} (latest dog_rank value
 * degraded by freshness). Runs every 15 days by default — 02:00 UTC on the 1st and 16th of each month —
 * override with the {@code k9x.dog-rank.cron} property.
 */
public class DogRankScheduler {

    private final UpdateDogRanksServiceCase updateDogRanksServiceCase;

    public DogRankScheduler(UpdateDogRanksServiceCase updateDogRanksServiceCase) {
        this.updateDogRanksServiceCase = updateDogRanksServiceCase;
    }

    @Scheduled(cron = "${k9x.dog-rank.cron:0 0 2 1,16 * *}", zone = "UTC")
    public void updateDogRanks() {
        updateDogRanksServiceCase.updateDogRanks();
    }
}
