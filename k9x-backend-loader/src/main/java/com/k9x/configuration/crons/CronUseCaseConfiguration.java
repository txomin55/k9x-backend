package com.k9x.configuration.crons;

import com.k9x.application.crons.use_case.RunCronServiceCase;
import com.k9x.application.dogs.rank.use_case.GenerateDogRankHistoryServiceCase;
import com.k9x.application.events.snapshot.use_case.GenerateEventSnapshotsServiceCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CronUseCaseConfiguration {

    @Bean
    public RunCronServiceCase runCronServiceCase(GenerateEventSnapshotsServiceCase generateEventSnapshotsServiceCase,
                                                 GenerateDogRankHistoryServiceCase generateDogRankHistoryServiceCase) {
        return new RunCronServiceCase(generateEventSnapshotsServiceCase, generateDogRankHistoryServiceCase);
    }
}
