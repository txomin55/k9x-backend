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
                new FetchDogRankEventResultDTO("dog-1", "OBDX", "evt-1", new BigDecimal("820.00"), first),
                new FetchDogRankEventResultDTO("dog-1", "OBDX", "evt-2", new BigDecimal("650.00"), now)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of());

        serviceCase.generateDogRankHistory();

        List<DogRankHistoryPayload> records = generatedRecords();
        assertThat(records).hasSize(2);
        assertThat(records.get(0).rank()).isEqualTo(820);
        assertThat(records.get(0).applyingTimestamp()).isEqualTo(first);
        assertThat(records.get(0).timestamp()).isGreaterThanOrEqualTo(now);
        assertThat(records.get(0).metadata())
                .isEqualTo(Map.of("type", "EVENT", "eventId", "evt-1"));
        // second record: index over both results at evt-2's time -> mean 735, freshness 1.0
        assertThat(records.get(1).rank()).isEqualTo(735);
        assertThat(records.get(1).metadata())
                .isEqualTo(Map.of("type", "EVENT", "eventId", "evt-2"));
    }

    @Test
    void appends_nothing_while_the_history_is_up_to_date_and_inside_the_plateau() {
        long now = DateUtils.nowUtcMillis();
        // ~9.5 months inactive: still inside the 10-month plateau, no degradation yet.
        long eventAt = now - 9 * MILLIS_PER_MONTH - 15 * MILLIS_PER_DAY;
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "OBDX", "evt-1", new BigDecimal("800.00"), eventAt)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of(
                new FetchLatestDogRankHistoryDTO("dog-1", "OBDX", 800, eventAt)));

        serviceCase.generateDogRankHistory();

        verify(createDogRankHistoryPersistencePort, never()).create(anyList());
    }

    @Test
    void appends_a_time_degradation_record_when_a_month_past_the_plateau_is_crossed() {
        long now = DateUtils.nowUtcMillis();
        // 10 months and a day inactive: first degradation record, slightly below the raw 800.
        long eventAt = now - 10 * MILLIS_PER_MONTH - MILLIS_PER_DAY;
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "OBDX", "evt-1", new BigDecimal("800.00"), eventAt)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of(
                new FetchLatestDogRankHistoryDTO("dog-1", "OBDX", 800, eventAt)));

        serviceCase.generateDogRankHistory();

        List<DogRankHistoryPayload> records = generatedRecords();
        assertThat(records).hasSize(1);
        DogRankHistoryPayload record = records.get(0);
        assertThat(record.rank()).isBetween(795, 799);
        assertThat(record.applyingTimestamp()).isGreaterThanOrEqualTo(now);
        assertThat(record.timestamp()).isGreaterThanOrEqualTo(now);
        assertThat(record.metadata()).isEqualTo(Map.of("type", "TIME_DEGRADATION", "month", "10"));
    }

    @Test
    void does_not_repeat_a_degradation_month_already_recorded() {
        long now = DateUtils.nowUtcMillis();
        long eventAt = now - 10 * MILLIS_PER_MONTH - 5 * MILLIS_PER_DAY;
        long degradedAt = now - 2 * MILLIS_PER_DAY; // month 10 already recorded three days after crossing
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "OBDX", "evt-1", new BigDecimal("800.00"), eventAt)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of(
                new FetchLatestDogRankHistoryDTO("dog-1", "OBDX", 799, degradedAt)));

        serviceCase.generateDogRankHistory();

        verify(createDogRankHistoryPersistencePort, never()).create(anyList());
    }

    @Test
    void a_new_event_resets_the_timeline_instead_of_degrading() {
        long now = DateUtils.nowUtcMillis();
        long oldEventAt = now - 12 * MILLIS_PER_MONTH;
        long degradedAt = now - MILLIS_PER_MONTH;
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "OBDX", "evt-1", new BigDecimal("800.00"), oldEventAt),
                new FetchDogRankEventResultDTO("dog-1", "OBDX", "evt-2", new BigDecimal("800.00"), now)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of(
                new FetchLatestDogRankHistoryDTO("dog-1", "OBDX", 736, degradedAt)));

        serviceCase.generateDogRankHistory();

        List<DogRankHistoryPayload> records = generatedRecords();
        assertThat(records).hasSize(1);
        // freshness snaps back to 1.0; level still averages the year-old 800 with the fresh 800 -> 800
        assertThat(records.get(0).rank()).isEqualTo(800);
        assertThat(records.get(0).metadata())
                .isEqualTo(Map.of("type", "EVENT", "eventId", "evt-2"));
    }

    @Test
    void each_discipline_carries_its_own_independent_timeline() {
        long now = DateUtils.nowUtcMillis();
        long agilityAt = now - 3 * MILLIS_PER_MONTH;
        // Weak in obedience (600, fresh) but strong in agility (850): one record per discipline, each with
        // its own index and dates — neither pollutes the other.
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "AGILITY", "evt-agility", new BigDecimal("850.00"), agilityAt),
                new FetchDogRankEventResultDTO("dog-1", "OBDX", "evt-obdx", new BigDecimal("600.00"), now)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of());

        serviceCase.generateDogRankHistory();

        List<DogRankHistoryPayload> records = generatedRecords();
        assertThat(records).hasSize(2);
        assertThat(records).anySatisfy(record -> {
            assertThat(record.discipline()).isEqualTo("AGILITY");
            assertThat(record.rank()).isEqualTo(850);
            assertThat(record.applyingTimestamp()).isEqualTo(agilityAt);
            assertThat(record.metadata()).isEqualTo(Map.of("type", "EVENT", "eventId", "evt-agility"));
        });
        assertThat(records).anySatisfy(record -> {
            assertThat(record.discipline()).isEqualTo("OBDX");
            assertThat(record.rank()).isEqualTo(600);
            assertThat(record.metadata()).isEqualTo(Map.of("type", "EVENT", "eventId", "evt-obdx"));
        });
    }

    @Test
    void time_degrades_every_discipline_against_its_own_last_event() {
        long now = DateUtils.nowUtcMillis();
        long obdxAt = now - 10 * MILLIS_PER_MONTH - MILLIS_PER_DAY;    // crossed the plateau -> degrades
        long agilityAt = now - 2 * MILLIS_PER_MONTH;                    // still fresh -> untouched
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "OBDX", "evt-obdx", new BigDecimal("800.00"), obdxAt),
                new FetchDogRankEventResultDTO("dog-1", "AGILITY", "evt-agility", new BigDecimal("850.00"), agilityAt)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of(
                new FetchLatestDogRankHistoryDTO("dog-1", "OBDX", 800, obdxAt),
                new FetchLatestDogRankHistoryDTO("dog-1", "AGILITY", 850, agilityAt)));

        serviceCase.generateDogRankHistory();

        List<DogRankHistoryPayload> records = generatedRecords();
        assertThat(records).hasSize(1);
        assertThat(records.get(0).discipline()).isEqualTo("OBDX");
        assertThat(records.get(0).rank()).isBetween(795, 799);
        assertThat(records.get(0).metadata()).isEqualTo(Map.of("type", "TIME_DEGRADATION", "month", "10"));
    }

    @Test
    void stops_degrading_once_the_curve_floor_month_is_recorded() {
        long now = DateUtils.nowUtcMillis();
        long eventAt = now - 80 * MILLIS_PER_MONTH;
        long floorRecordedAt = eventAt + 71 * MILLIS_PER_MONTH; // month 70+ already recorded
        when(getDogRankEventResultsPersistencePort.getEventResults()).thenReturn(List.of(
                new FetchDogRankEventResultDTO("dog-1", "OBDX", "evt-1", new BigDecimal("800.00"), eventAt)));
        when(getLatestDogRankHistoryPersistencePort.getLatestHistory()).thenReturn(List.of(
                new FetchLatestDogRankHistoryDTO("dog-1", "OBDX", 8, floorRecordedAt)));

        serviceCase.generateDogRankHistory();

        verify(createDogRankHistoryPersistencePort, never()).create(anyList());
    }
}
