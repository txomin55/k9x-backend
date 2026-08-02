package com.k9x.configuration.dogrank;

import com.k9x.application.dogs.rank.use_case.UpdateDogRanksServiceCase;
import com.k9x.infrastructure.in.scheduler.DogRankScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class DogRankSchedulerConfiguration {

    @Bean
    public DogRankScheduler dogRankScheduler(UpdateDogRanksServiceCase updateDogRanksServiceCase) {
        return new DogRankScheduler(updateDogRanksServiceCase);
    }
}
