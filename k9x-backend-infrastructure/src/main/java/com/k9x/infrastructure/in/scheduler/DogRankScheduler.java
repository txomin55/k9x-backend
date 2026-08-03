package com.k9x.infrastructure.in.scheduler;

import com.k9x.application.dogs.rank.use_case.GenerateDogRankHistoryServiceCase;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Inbound adapter that triggers the periodic append to {@code k9x.snap_dog_index_history} (the competitor index
 * timeline: new event results and inactivity degradations). Runs every 15 days by default — 02:00 UTC on the
 * 1st and 16th of each month — override with the {@code k9x.dog-rank.cron} property.
 */
public class DogRankScheduler {

    private final GenerateDogRankHistoryServiceCase generateDogRankHistoryServiceCase;

    public DogRankScheduler(GenerateDogRankHistoryServiceCase generateDogRankHistoryServiceCase) {
        this.generateDogRankHistoryServiceCase = generateDogRankHistoryServiceCase;
    }

    @Scheduled(cron = "${k9x.dog-rank.cron:0 0 2 1,16 * *}", zone = "UTC")
    public void generateDogRankHistory() {
        generateDogRankHistoryServiceCase.generateDogRankHistory();
    }
}
