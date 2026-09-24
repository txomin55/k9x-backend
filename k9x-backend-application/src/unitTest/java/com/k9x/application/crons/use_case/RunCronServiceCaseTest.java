package com.k9x.application.crons.use_case;

import com.k9x.application.crons.use_case.command.CronJob;
import com.k9x.application.dogs.rank.use_case.GenerateDogRankHistoryServiceCase;
import com.k9x.application.events.snapshot.use_case.GenerateEventSnapshotsServiceCase;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RunCronServiceCaseTest {

    private static final String SUPPORT = "k9x.support@gmail.com";

    @Mock
    GenerateEventSnapshotsServiceCase generateEventSnapshotsServiceCase;
    @Mock
    GenerateDogRankHistoryServiceCase generateDogRankHistoryServiceCase;

    private RunCronServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new RunCronServiceCase(generateEventSnapshotsServiceCase, generateDogRankHistoryServiceCase);
    }

    @Test
    void the_support_account_runs_the_snapshots_and_gets_the_count() {
        when(generateEventSnapshotsServiceCase.generateSnapshots()).thenReturn(16);

        assertThat(serviceCase.run(CronJob.SNAPSHOTS, SUPPORT)).isEqualTo(16);
        verify(generateDogRankHistoryServiceCase, never()).generateDogRankHistory();
    }

    @Test
    void the_support_account_runs_the_dog_rank_history_and_gets_the_count() {
        when(generateDogRankHistoryServiceCase.generateDogRankHistory()).thenReturn(42);

        assertThat(serviceCase.run(CronJob.DOG_RANK_HISTORY, SUPPORT)).isEqualTo(42);
        verify(generateEventSnapshotsServiceCase, never()).generateSnapshots();
    }

    @Test
    void anybody_else_is_rejected_without_running_anything() {
        assertThatThrownBy(() -> serviceCase.run(CronJob.SNAPSHOTS, "someone@example.com"))
                .isInstanceOf(UnauthorizedResourceException.class);
        verifyNoInteractions(generateEventSnapshotsServiceCase, generateDogRankHistoryServiceCase);
    }

    @Test
    void the_query_names_map_to_their_job_and_nothing_else() {
        assertThat(CronJob.fromParam("snapshots")).contains(CronJob.SNAPSHOTS);
        assertThat(CronJob.fromParam("dog-rank-history")).contains(CronJob.DOG_RANK_HISTORY);
        assertThat(CronJob.fromParam("whatever")).isEmpty();
    }
}
