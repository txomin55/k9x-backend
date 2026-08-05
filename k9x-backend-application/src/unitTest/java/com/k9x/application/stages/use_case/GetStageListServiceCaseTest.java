package com.k9x.application.stages.use_case;

import com.k9x.application.notifications.port.GetStageNotificationsPersistencePort;
import com.k9x.application.stages.port.GetStageListPersistencePort;
import com.k9x.application.stages.use_case.dto.FetchStageListDTO;
import com.k9x.application.stages.use_case.dto.FetchStageListEventDTO;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.EventJudge;
import com.k9x.domain.events.valueobjects.Score;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStageListServiceCaseTest {

    private static final long FAR_FUTURE = 4_000_000_000_000L; // year 2096
    private static final long FAR_PAST = 1_000L;               // 1970

    @Mock
    private GetStageListPersistencePort getStageListPersistencePort;

    @Mock
    private GetStageNotificationsPersistencePort getStageNotificationsPersistencePort;

    private GetStageListServiceCase serviceCase;

    private static CompetitionSnapshot competition(StageSnapshot stage) {
        return competition(List.of(stage));
    }

    private static CompetitionSnapshot competition(List<StageSnapshot> stages) {
        return new CompetitionSnapshot("comp", "Comp", "creator", "Organizer Name", "ES",
                "desc", "Calle Mayor 1", 40.4, -3.7, 0L, 0L, null, stages);
    }

    private static StageSnapshot stage(String id, long from, long to, List<EventSnapshot> events) {
        return new StageSnapshot(id, "Stage " + id, "comp", "creator", from, to, 0L, 0L, null, events);
    }

    private static EventSnapshot event(String id, String configId, Long deletedAt, List<EventCompetitor> competitors,
                                       List<EventExercise> exercises, List<EventJudge> judges, List<Score> scores) {
        return new EventSnapshot(id, configId, "OBDX", "Event " + id, "s-1", "creator",
                null, 0L, 0L, deletedAt, ObdxAvgMethod.AVG, competitors, exercises, judges, scores, List.of(), null, null);
    }

    private static EventCompetitor competitor(String dogId, boolean notCompeting) {
        return new EventCompetitor(dogId, "Rex", "owner", "Handler", "Team A", "ES", "Border Collie", "ID-001",
                (short) 1, null, true, notCompeting, null, null, null);
    }

    private static EventExercise exercise(String id) {
        return new EventExercise(id, (short) 1, List.of(), List.of("j-1"));
    }

    private static EventJudge judge(String id) {
        return new EventJudge(id, "Judge " + id, "collector@test.com");
    }

    @BeforeEach
    void setUp() {
        serviceCase = new GetStageListServiceCase(getStageListPersistencePort, getStageNotificationsPersistencePort);
    }

    @Test
    void surfaces_discipline_id_and_computes_finished_stage_and_event_when_unscored() {
        EventSnapshot event = event("evt-1", "obdx-1", null, List.of(competitor("dog-1", false)),
                List.of(exercise("ex-1")), List.of(judge("j-1")), List.of());
        CompetitionSnapshot competition = competition(stage("s-1", FAR_PAST, FAR_PAST, List.of(event)));

        when(getStageListPersistencePort.getCompetitions()).thenReturn(List.of(competition));

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
        // 2 exercises x 1 judge = 2 required scores, only 1 recorded -> competitor not settled (not FINISHED),
        // but a score exists -> event STARTED. dateTo in the far future rules out the date-driven FINISHED.
        EventSnapshot event = event("evt-1", "obdx-1", null, List.of(competitor("dog-1", false)),
                List.of(exercise("ex-1"), exercise("ex-2")), List.of(judge("j-1")),
                List.of(new Score("ex-1", "j-1", "dog-1", new BigDecimal("7.0"), 0L)));
        CompetitionSnapshot competition = competition(stage("s-1", FAR_PAST, FAR_FUTURE, List.of(event)));

        when(getStageListPersistencePort.getCompetitions()).thenReturn(List.of(competition));

        List<FetchStageListDTO> result = serviceCase.getStages(null, null);

        assertThat(result.getFirst().events().getFirst().status()).isEqualTo("STARTED");
        assertThat(result.getFirst().status()).isEqualTo("STARTED");
    }

    @Test
    void enrollment_is_closed_when_event_has_no_deadline() {
        EventSnapshot event = event("evt-1", "obdx-1", null, List.of(), List.of(), List.of(), List.of());
        CompetitionSnapshot competition = competition(stage("s-1", FAR_FUTURE, FAR_FUTURE, List.of(event)));

        when(getStageListPersistencePort.getCompetitions()).thenReturn(List.of(competition));

        List<FetchStageListDTO> result = serviceCase.getStages(null, null);

        assertThat(result.getFirst().events().getFirst().enrollmentOpened()).isFalse();
        assertThat(result.getFirst().events().getFirst().enrollmentDeadline()).isNull();
    }

    @Test
    void orders_upcoming_ascending_then_past_descending() {
        long pastOld = FAR_PAST;                    // 1970
        long pastRecent = 1_500_000_000_000L;       // 2017
        long upcomingSoon = 3_000_000_000_000L;     // 2065
        long upcomingFar = FAR_FUTURE;              // 2096

        StageSnapshot sPastOld = stage("past-old", pastOld, pastOld, List.of());
        StageSnapshot sPastRecent = stage("past-recent", pastRecent, pastRecent, List.of());
        StageSnapshot sUpcomingSoon = stage("upcoming-soon", upcomingSoon, upcomingSoon, List.of());
        StageSnapshot sUpcomingFar = stage("upcoming-far", upcomingFar, upcomingFar, List.of());
        CompetitionSnapshot competition = competition(
                List.of(sPastOld, sUpcomingFar, sPastRecent, sUpcomingSoon));

        when(getStageListPersistencePort.getCompetitions()).thenReturn(List.of(competition));

        List<FetchStageListDTO> result = serviceCase.getStages(null, null);

        // Upcoming/ongoing first (soonest first), then past (most recent first).
        assertThat(result).extracting(FetchStageListDTO::id)
                .containsExactly("upcoming-soon", "upcoming-far", "past-recent", "past-old");
    }

    @Test
    void filters_stages_by_date_from_within_range() {
        long before = 1_000_000_000_000L; // 2001
        long inside = 2_000_000_000_000L;  // 2033
        long after = 3_000_000_000_000L;   // 2065
        CompetitionSnapshot competition = competition(List.of(
                stage("before", before, before, List.of()),
                stage("inside", inside, inside, List.of()),
                stage("after", after, after, List.of())));

        when(getStageListPersistencePort.getCompetitions()).thenReturn(List.of(competition));

        List<FetchStageListDTO> result = serviceCase.getStages(before + 1, after - 1);

        assertThat(result).extracting(FetchStageListDTO::id).containsExactly("inside");
    }

    @Test
    void treats_null_range_bounds_as_open_ended() {
        long early = 1_000_000_000_000L; // 2001
        long late = 3_000_000_000_000L;  // 2065
        CompetitionSnapshot competition = competition(List.of(
                stage("early", early, early, List.of()),
                stage("late", late, late, List.of())));

        when(getStageListPersistencePort.getCompetitions()).thenReturn(List.of(competition));

        assertThat(serviceCase.getStages(null, early).stream().map(FetchStageListDTO::id).toList())
                .containsExactly("early");
        assertThat(serviceCase.getStages(late, null).stream().map(FetchStageListDTO::id).toList())
                .containsExactly("late");
    }

    @Test
    void skips_deleted_stages_and_deleted_events() {
        EventSnapshot liveEvent = event("evt-1", "obdx-1", null, List.of(), List.of(), List.of(), List.of());
        EventSnapshot deletedEvent = event("evt-2", "obdx-1", 999L, List.of(), List.of(), List.of(), List.of());
        StageSnapshot liveStage = stage("s-1", FAR_PAST, FAR_PAST, List.of(liveEvent, deletedEvent));
        StageSnapshot deletedStage = new StageSnapshot("s-2", "Stage s-2", "comp", "creator",
                FAR_PAST, FAR_PAST, 0L, 0L, 888L, List.of(liveEvent));
        CompetitionSnapshot competition = competition(List.of(liveStage, deletedStage));

        when(getStageListPersistencePort.getCompetitions()).thenReturn(List.of(competition));

        List<FetchStageListDTO> result = serviceCase.getStages(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("s-1");
        assertThat(result.getFirst().events()).extracting(FetchStageListEventDTO::id).containsExactly("evt-1");
    }
}
