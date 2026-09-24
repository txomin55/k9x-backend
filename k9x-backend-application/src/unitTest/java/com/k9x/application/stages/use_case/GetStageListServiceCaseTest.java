package com.k9x.application.stages.use_case;

import com.k9x.application.notifications.port.GetStageNotificationsPersistencePort;
import com.k9x.application.rankings.port.GetRankedEventIdsPersistencePort;
import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageListDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListEventDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListRowDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListRowEventDTO;
import com.k9x.application.utils.date.DateUtils;
import com.k9x.domain.disciplines.obdx.ObdxRank;
import com.k9x.domain.shared.UtcDates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStageListServiceCaseTest {

    private static final long FAR_FUTURE = 4_000_000_000_000L; // year 2096
    private static final long FAR_PAST = 1_000L;               // 1970

    @Mock
    private GetStageListPersistencePort getStageListPersistencePort;

    @Mock
    private GetStageNotificationsPersistencePort getStageNotificationsPersistencePort;

    @Mock
    private GetRankedEventIdsPersistencePort getRankedEventIdsPersistencePort;

    private GetStageListServiceCase serviceCase;

    private static FetchStageListRowDTO stage(String id, long from, long to, List<FetchStageListRowEventDTO> events) {
        return new FetchStageListRowDTO(id, "Stage " + id, from, to, "Comp", "ES", "Calle Mayor 1", 40.4, -3.7,
                "Organizer Name", null, events);
    }

    private static FetchStageListRowEventDTO event(String id, Long deletedAt, int competitors, boolean hasAnyScore,
                                                   boolean allSettled) {
        return new FetchStageListRowEventDTO(id, "Event " + id, "OBDX", deletedAt, null, List.of(), null,
                competitors, hasAnyScore, allSettled);
    }

    private void listReturns(FetchStageListRowDTO... stages) {
        when(getStageListPersistencePort.getStages(any(), any(), anyLong())).thenReturn(List.of(stages));
    }

    @BeforeEach
    void setUp() {
        serviceCase = new GetStageListServiceCase(getStageListPersistencePort, getStageNotificationsPersistencePort,
                getRankedEventIdsPersistencePort);
    }

    @Test
    void pushes_the_date_range_and_the_start_of_today_down_to_the_query() {
        long before = UtcDates.startOfUtcDay(DateUtils.nowUtcMillis());
        when(getStageListPersistencePort.getStages(eq(10L), isNull(), anyLong())).thenReturn(List.of());

        serviceCase.getStages(10L, null);

        ArgumentCaptor<Long> startOfToday = ArgumentCaptor.forClass(Long.class);
        verify(getStageListPersistencePort).getStages(eq(10L), isNull(), startOfToday.capture());
        assertThat(startOfToday.getValue()).isEqualTo(UtcDates.startOfUtcDay(startOfToday.getValue()))
                .isGreaterThanOrEqualTo(before);
    }

    @Test
    void surfaces_discipline_id_and_computes_finished_stage_and_event_when_unscored() {
        listReturns(stage("s-1", FAR_PAST, FAR_PAST, List.of(event("evt-1", null, 1, false, false))));

        List<FetchStageListDTO> result = serviceCase.getStages(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().events()).hasSize(1);
        assertThat(result.getFirst().events().getFirst().disciplineId()).isEqualTo("OBDX");
        assertThat(result.getFirst().events().getFirst().competitorCount()).isEqualTo(1);
        // dateTo in 1970 is before today's UTC day -> stage and its events are FINISHED, even unscored.
        assertThat(result.getFirst().events().getFirst().status()).isEqualTo("FINISHED");
        assertThat(result.getFirst().status()).isEqualTo("FINISHED");
    }

    @Test
    void surfaces_started_status_when_an_event_holds_a_score() {
        // A score exists but not every competitor is settled -> event STARTED. dateTo in the far future rules
        // out the date-driven FINISHED.
        listReturns(stage("s-1", FAR_PAST, FAR_FUTURE, List.of(event("evt-1", null, 1, true, false))));

        List<FetchStageListDTO> result = serviceCase.getStages(null, null);

        assertThat(result.getFirst().events().getFirst().status()).isEqualTo("STARTED");
        assertThat(result.getFirst().status()).isEqualTo("STARTED");
    }

    @Test
    void a_running_event_with_every_competitor_settled_is_finished_before_its_date() {
        listReturns(stage("s-1", FAR_PAST, FAR_FUTURE, List.of(event("evt-1", null, 1, true, true))));

        List<FetchStageListDTO> result = serviceCase.getStages(null, null);

        assertThat(result.getFirst().events().getFirst().status()).isEqualTo("FINISHED");
        assertThat(result.getFirst().status()).isEqualTo("FINISHED");
    }

    @Test
    void enrollment_is_closed_when_event_has_no_deadline() {
        listReturns(stage("s-1", FAR_FUTURE, FAR_FUTURE, List.of(event("evt-1", null, 0, false, false))));

        List<FetchStageListDTO> result = serviceCase.getStages(null, null);

        assertThat(result.getFirst().events().getFirst().enrollmentOpened()).isFalse();
        assertThat(result.getFirst().events().getFirst().enrollmentDeadline()).isNull();
    }

    @Test
    void enrollment_follows_the_deadline_while_the_stage_is_not_under_way() {
        FetchStageListRowEventDTO open = new FetchStageListRowEventDTO("evt-1", "Event", "OBDX", null, FAR_FUTURE,
                List.of(), null, 0, false, false);
        listReturns(stage("s-1", FAR_FUTURE, FAR_FUTURE, List.of(open)));

        List<FetchStageListDTO> result = serviceCase.getStages(null, null);

        assertThat(result.getFirst().status()).isEqualTo("CREATED");
        assertThat(result.getFirst().events().getFirst().enrollmentOpened()).isTrue();
    }

    @Test
    void derives_the_rank_label_from_the_rank_score() {
        FetchStageListRowEventDTO ranked = new FetchStageListRowEventDTO("evt-1", "Event", "OBDX", null, null,
                List.of(), 900, 0, false, false);
        listReturns(stage("s-1", FAR_PAST, FAR_PAST, List.of(ranked)));

        FetchStageListEventDTO event = serviceCase.getStages(null, null).getFirst().events().getFirst();

        assertThat(event.rank()).isEqualTo(ObdxRank.labelFromScore(900));
    }

    @Test
    void orders_upcoming_ascending_then_past_descending() {
        long pastOld = FAR_PAST;                    // 1970
        long pastRecent = 1_500_000_000_000L;       // 2017
        long upcomingSoon = 3_000_000_000_000L;     // 2065
        long upcomingFar = FAR_FUTURE;              // 2096
        listReturns(stage("past-old", pastOld, pastOld, List.of()),
                stage("upcoming-far", upcomingFar, upcomingFar, List.of()),
                stage("past-recent", pastRecent, pastRecent, List.of()),
                stage("upcoming-soon", upcomingSoon, upcomingSoon, List.of()));

        List<FetchStageListDTO> result = serviceCase.getStages(null, null);

        // Upcoming/ongoing first (soonest first), then past (most recent first).
        assertThat(result).extracting(FetchStageListDTO::id)
                .containsExactly("upcoming-soon", "upcoming-far", "past-recent", "past-old");
    }

    @Test
    void lists_only_active_events_but_a_deleted_one_still_keeps_the_stage_from_finishing() {
        // The active event is settled, the deleted one is DELETED: not every event is FINISHED, so the stage
        // stays STARTED until its date passes, while the list only shows the active event.
        listReturns(stage("s-1", FAR_PAST, FAR_FUTURE, List.of(
                event("evt-1", null, 1, true, true),
                event("evt-2", 999L, 0, false, false))));

        FetchStageListDTO result = serviceCase.getStages(null, null).getFirst();

        assertThat(result.events()).extracting(FetchStageListEventDTO::id).containsExactly("evt-1");
        assertThat(result.status()).isEqualTo("STARTED");
    }

    @Test
    void flags_stages_with_a_ranked_active_event() {
        listReturns(stage("s-1", FAR_PAST, FAR_PAST, List.of(event("evt-1", null, 0, false, false))),
                stage("s-2", FAR_PAST, FAR_PAST, List.of(event("evt-2", null, 0, false, false))));
        when(getRankedEventIdsPersistencePort.getRankedEventIds()).thenReturn(Set.of("evt-2"));

        List<FetchStageListDTO> result = serviceCase.getStages(null, null);

        assertThat(result).filteredOn(s -> s.id().equals("s-2")).singleElement()
                .extracting(FetchStageListDTO::includesRankings).isEqualTo(true);
        assertThat(result).filteredOn(s -> s.id().equals("s-1")).singleElement()
                .extracting(FetchStageListDTO::includesRankings).isEqualTo(false);
    }
}
