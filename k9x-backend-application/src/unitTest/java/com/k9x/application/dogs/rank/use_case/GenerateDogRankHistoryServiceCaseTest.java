package com.k9x.application.dogs.rank.use_case;

import com.k9x.application.dogs.rank.port.CreateDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.GetDogRankEventResultsPersistencePort;
import com.k9x.application.dogs.rank.port.GetLatestDogRankHistoryPersistencePort;
import com.k9x.application.dogs.rank.port.payload.DogRankHistoryPayload;
import com.k9x.application.dogs.rank.use_case.dto.FetchDogRankEventResultDTO;
import com.k9x.application.dogs.rank.use_case.dto.FetchLatestDogRankHistoryDTO;
import com.k9x.application.utils.date.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateDogRankHistoryServiceCaseTest {

    private static final long MILLIS_PER_MONTH = (long) (30.4375 * 86_400_000.0);
    private static final long MILLIS_PER_DAY = 86_400_000L;

    @Mock
    GetDogRankEventResultsPersistencePort getDogRankEventResultsPersistencePort;
    @Mock
    GetLatestDogRankHistoryPersistencePort getLatestDogRankHistoryPersistencePort;
    @Mock
    CreateDogRankHistoryPersistencePort createDogRankHistoryPersistencePort;

    private GenerateDogRankHistoryServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GenerateDogRankHistoryServiceCase(getDogRankEventResultsPersistencePort,
                getLatestDogRankHistoryPersistencePort, createDogRankHistoryPersistencePort);
    }

    private List<DogRankHistoryPayload> generatedRecords() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DogRankHistoryPayload>> captor = ArgumentCaptor.forClass(List.class);
        verify(createDogRankHistoryPersistencePort).create(captor.capture());
        return captor.getValue();
    }

    @Test
    void appends_one_event_record_per_result_not_yet_in_the_history() {
        long now = DateUtils.nowUtcMillis();
        long first = now - 3 * MILLIS_PER_MONTH;
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "evt-1", new BigDecimal("820.00"), first),
                new FetchDogRankEventResultDTO("dog-1", "evt-2", new BigDecimal("650.00"), now)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of());

        serviceCase.generateDogRankHistory();

        List<DogRankHistoryPayload> records = generatedRecords();
        assertThat(records).hasSize(2);
        // first record: only one of the three level slots filled -> (820 + 201 + 201) / 3
        assertThat(records.get(0).rank()).isEqualTo(407);
        assertThat(records.get(0).applyingTimestamp()).isEqualTo(first);
        assertThat(records.get(0).timestamp()).isGreaterThanOrEqualTo(now);
        assertThat(records.get(0).metadata())
                .isEqualTo(Map.of("type", "EVENT", "eventId", "evt-1"));
        // second record: two slots filled at evt-2's time -> (820 + 650 + 201) / 3, freshness 1.0
        assertThat(records.get(1).rank()).isEqualTo(557);
        assertThat(records.get(1).metadata())
                .isEqualTo(Map.of("type", "EVENT", "eventId", "evt-2"));
    }

    @Test
    void appends_nothing_while_the_history_is_up_to_date_and_inside_the_plateau() {
        long now = DateUtils.nowUtcMillis();
        // ~5.5 months inactive: still inside the 6-month freshness plateau, no degradation yet.
        long eventAt = now - 5 * MILLIS_PER_MONTH - 15 * MILLIS_PER_DAY;
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "evt-1", new BigDecimal("800.00"), eventAt)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of(
                new FetchLatestDogRankHistoryDTO("dog-1", 800, eventAt)));

        serviceCase.generateDogRankHistory();

        verify(createDogRankHistoryPersistencePort, never()).create(anyList());
    }

    @Test
    void appends_a_time_degradation_record_when_a_month_past_the_plateau_is_crossed() {
        long now = DateUtils.nowUtcMillis();
        // 6 months and a day inactive: first degradation record, just off the plateau.
        long eventAt = now - 6 * MILLIS_PER_MONTH - MILLIS_PER_DAY;
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "evt-1", new BigDecimal("800.00"), eventAt)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of(
                new FetchLatestDogRankHistoryDTO("dog-1", 401, eventAt)));

        serviceCase.generateDogRankHistory();

        List<DogRankHistoryPayload> records = generatedRecords();
        assertThat(records).hasSize(1);
        DogRankHistoryPayload record = records.get(0);
        // one slot filled -> level 400.67, freshness barely off 1.0
        assertThat(record.rank()).isBetween(396, 401);
        assertThat(record.applyingTimestamp()).isGreaterThanOrEqualTo(now);
        assertThat(record.timestamp()).isGreaterThanOrEqualTo(now);
        assertThat(record.metadata()).isEqualTo(Map.of("type", "TIME_DEGRADATION", "month", "6"));
    }

    @Test
    void does_not_repeat_a_degradation_month_already_recorded() {
        long now = DateUtils.nowUtcMillis();
        long eventAt = now - 6 * MILLIS_PER_MONTH - 5 * MILLIS_PER_DAY;
        long degradedAt = now - 2 * MILLIS_PER_DAY; // month 6 already recorded three days after crossing
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "evt-1", new BigDecimal("800.00"), eventAt)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of(
                new FetchLatestDogRankHistoryDTO("dog-1", 400, degradedAt)));

        serviceCase.generateDogRankHistory();

        verify(createDogRankHistoryPersistencePort, never()).create(anyList());
    }

    @Test
    void a_new_event_resets_the_timeline_instead_of_degrading() {
        long now = DateUtils.nowUtcMillis();
        long oldEventAt = now - 12 * MILLIS_PER_MONTH;
        long degradedAt = now - MILLIS_PER_MONTH;
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "evt-1", new BigDecimal("800.00"), oldEventAt),
                new FetchDogRankEventResultDTO("dog-1", "evt-2", new BigDecimal("800.00"), now)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of(
                new FetchLatestDogRankHistoryDTO("dog-1", 294, degradedAt)));

        serviceCase.generateDogRankHistory();

        List<DogRankHistoryPayload> records = generatedRecords();
        assertThat(records).hasSize(1);
        // freshness snaps back to 1.0; the year-old 800 keeps 0.90 of its weight -> (800 + 720 + 201) / 3
        assertThat(records.get(0).rank()).isEqualTo(574);
        assertThat(records.get(0).metadata())
                .isEqualTo(Map.of("type", "EVENT", "eventId", "evt-2"));
    }

    @Test
    void every_discipline_feeds_the_same_single_timeline() {
        long now = DateUtils.nowUtcMillis();
        long agilityAt = now - 3 * MILLIS_PER_MONTH;
        // Agility and obedience results are not separated: both fill the same level slots on the shared
        // 0-1000 scale, so the dog carries one timeline whatever it competed in.
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "evt-agility", new BigDecimal("850.00"), agilityAt),
                new FetchDogRankEventResultDTO("dog-1", "evt-obdx", new BigDecimal("600.00"), now)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of());

        serviceCase.generateDogRankHistory();

        List<DogRankHistoryPayload> records = generatedRecords();
        assertThat(records).hasSize(2);
        // only the agility result yet -> (850 + 201 + 201) / 3
        assertThat(records.get(0).rank()).isEqualTo(417);
        assertThat(records.get(0).applyingTimestamp()).isEqualTo(agilityAt);
        assertThat(records.get(0).metadata()).isEqualTo(Map.of("type", "EVENT", "eventId", "evt-agility"));
        // the obedience result joins the very same slots -> (850 + 600 + 201) / 3, both inside the plateau
        assertThat(records.get(1).rank()).isEqualTo(550);
        assertThat(records.get(1).metadata()).isEqualTo(Map.of("type", "EVENT", "eventId", "evt-obdx"));
    }

    @Test
    void inactivity_is_measured_against_the_last_event_in_any_discipline() {
        long now = DateUtils.nowUtcMillis();
        long obdxAt = now - 6 * MILLIS_PER_MONTH - MILLIS_PER_DAY;     // would have crossed the plateau alone
        long agilityAt = now - 2 * MILLIS_PER_MONTH;                    // but the dog did compete recently
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "evt-obdx", new BigDecimal("800.00"), obdxAt),
                new FetchDogRankEventResultDTO("dog-1", "evt-agility", new BigDecimal("850.00"), agilityAt)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of(
                new FetchLatestDogRankHistoryDTO("dog-1", 617, agilityAt)));

        serviceCase.generateDogRankHistory();

        verify(createDogRankHistoryPersistencePort, never()).create(anyList());
    }

    @Test
    void stops_degrading_once_the_curve_floor_month_is_recorded() {
        long now = DateUtils.nowUtcMillis();
        long eventAt = now - 70 * MILLIS_PER_MONTH;
        long floorRecordedAt = eventAt + 59 * MILLIS_PER_MONTH; // the freshness floor month (58) already recorded
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "evt-1", new BigDecimal("800.00"), eventAt)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of(
                new FetchLatestDogRankHistoryDTO("dog-1", 1, floorRecordedAt)));

        serviceCase.generateDogRankHistory();

        verify(createDogRankHistoryPersistencePort, never()).create(anyList());
    }
}
