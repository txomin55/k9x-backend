package com.k9x.application.dogs.rank.use_case;

import com.k9x.application.dogs.rank.port.GetDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.UpdateDogRanksPersistencePort;
import com.k9x.application.dogs.rank.port.payload.DogRankUpdatePayload;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankDTO;
import com.k9x.application.utils.date.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateDogRanksServiceCaseTest {

    private static final long MILLIS_PER_MONTH = (long) (30.4375 * 86_400_000.0);

    @Mock
    GetDogRankHistoryPersistencePort getDogRankHistoryPersistencePort;
    @Mock
    UpdateDogRanksPersistencePort updateDogRanksPersistencePort;

    private UpdateDogRanksServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new UpdateDogRanksServiceCase(getDogRankHistoryPersistencePort, updateDogRanksPersistencePort);
    }

    @Test
    void does_not_touch_dogs_when_there_is_no_rank_history() {
        when(getDogRankHistoryPersistencePort.getDogRankHistory(anyString())).thenReturn(List.of());

        serviceCase.updateDogRanks();

        verify(updateDogRanksPersistencePort, never()).updateRanks(anyList());
    }

    @Test
    void updates_each_dog_with_its_level_times_freshness_index() {
        long now = DateUtils.nowUtcMillis();
        when(getDogRankHistoryPersistencePort.getDogRankHistory("OBDX")).thenReturn(List.of(
                // dog-steady: 820 (6 months), 810 (3 months), 650 (fresh) -> plain mean 760, freshness 1.0
                new FetchDogRankDTO("dog-steady", new BigDecimal("820.00"), now - 6 * MILLIS_PER_MONTH),
                new FetchDogRankDTO("dog-steady", new BigDecimal("810.00"), now - 3 * MILLIS_PER_MONTH),
                new FetchDogRankDTO("dog-steady", new BigDecimal("650.00"), now),
                // dog-inactive: single 700, 34 months old -> level 700 × freshness 0.5
                new FetchDogRankDTO("dog-inactive", new BigDecimal("700.00"), now - 34 * MILLIS_PER_MONTH)));

        serviceCase.updateDogRanks();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DogRankUpdatePayload>> captor = ArgumentCaptor.forClass(List.class);
        verify(updateDogRanksPersistencePort).updateRanks(captor.capture());
        List<DogRankUpdatePayload> updates = captor.getValue();
        assertThat(updates).hasSize(2);
        assertThat(updates.get(0).dogId()).isEqualTo("dog-steady");
        assertThat(updates.get(0).rank()).isEqualTo(760);
        assertThat(updates.get(1).dogId()).isEqualTo("dog-inactive");
        assertThat(updates.get(1).rank()).isEqualTo(350);
    }
}
