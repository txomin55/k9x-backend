package com.k9x.application.competitions.use_case;

import com.k9x.application.competitions.port.GetCompetitionListPersistencePort;
import com.k9x.application.notifications.port.GetStageNotificationsPersistencePort;
import com.k9x.application.competitions.use_case.dto.FetchCompetitionDTO;
import com.k9x.application.competitions.use_case.dto.FetchEventDTO;
import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.events.aggregates.EventSnapshot;
import com.k9x.domain.events.valueobjects.EventCompetitor;
import com.k9x.domain.events.valueobjects.EventExercise;
import com.k9x.domain.events.valueobjects.EventJudge;
import com.k9x.domain.events.valueobjects.Score;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCompetitionListServiceCaseTest {

    @Mock
    private GetCompetitionListPersistencePort getCompetitionListPersistencePort;

    @Mock
    private GetStageNotificationsPersistencePort getStageNotificationsPersistencePort;

    private GetCompetitionListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetCompetitionListServiceCase(getCompetitionListPersistencePort,
                getStageNotificationsPersistencePort);
    }

    private CompetitionSnapshot competition(String id, List<StageSnapshot> stages) {
        return new CompetitionSnapshot(id, "World Cup", "user-1", "Org", "ES", "desc", "Calle Mayor 1",
                null, null, 0L, 0L, null, stages);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.getCompetitions("user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getCompetitionListPersistencePort);
    }

    @Test
    void maps_empty_competition_as_created() {
        when(getCompetitionListPersistencePort.getCompetitions("user-1"))
                .thenReturn(List.of(competition("comp-1", List.of())));

        List<FetchCompetitionDTO> result = serviceCase.getCompetitions("user-1", true);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("comp-1");
        assertThat(result.getFirst().status()).isEqualTo("CREATED");
        assertThat(result.getFirst().stages()).isEmpty();
        verify(getCompetitionListPersistencePort).getCompetitions("user-1");
    }

    @Test
    void maps_event_status_from_domain_logic() {
        // created event: no scores -> CREATED. started event: one recorded score -> STARTED.
        EventSnapshot createdEvent = event("event-created", List.of(), List.of(), List.of(), List.of());
        EventCompetitor competitor = new EventCompetitor("dog-1", "Rex", "owner", "Handler", "team", "ES", "breed",
                "id-1", null, (short) 1, null, true, false, null, null, null);
        // two judges assigned to the exercise but only one scored -> a score exists yet the competitor
        // is not settled -> STARTED.
        EventExercise exercise = new EventExercise("ex-1", (short) 1, null, List.of("judge-1", "judge-2"));
        List<EventJudge> startedJudges = List.of(new EventJudge("judge-1", "Judge", null),
                new EventJudge("judge-2", "Judge 2", null));
        EventSnapshot startedEvent = event("event-started", List.of(competitor), List.of(exercise),
                startedJudges, List.of(new Score("ex-1", "judge-1", "dog-1", new BigDecimal("8"), 0L)));
        StageSnapshot stage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1",
                0L, Long.MAX_VALUE, 0L, 0L, null, List.of(createdEvent, startedEvent));
        when(getCompetitionListPersistencePort.getCompetitions("user-1"))
                .thenReturn(List.of(competition("comp-1", List.of(stage))));

        List<FetchEventDTO> events = serviceCase.getCompetitions("user-1", true)
                .getFirst().stages().getFirst().events();

        assertThat(events).hasSize(2);
        assertThat(events.get(0).status()).isEqualTo("CREATED");
        assertThat(events.get(1).status()).isEqualTo("STARTED");
    }

    private EventSnapshot event(String id, List<EventCompetitor> competitors, List<EventExercise> exercises,
                                List<EventJudge> judges, List<Score> scores) {
        return new EventSnapshot(id, "cfg-1", "OBDX", id, "stage-1", "user-1", null, 0L, 0L, null,
                ObdxAvgMethod.MID_AVG, competitors, exercises, judges, scores, List.of(), null, null);
    }

    @Test
    void orders_competitions_by_nearest_stage_proximity() {
        long now = com.k9x.application.utils.date.DateUtils.nowUtcMillis();
        long day = 24L * 60 * 60 * 1000;
        // upcoming-soon (nearest upcoming) should win, then upcoming-later, then most-recent-past,
        // then older-past, then a competition with no stages.
        CompetitionSnapshot upcomingLater = competition("upcoming-later", List.of(stage(now + 30 * day)));
        CompetitionSnapshot upcomingSoon = competition("upcoming-soon", List.of(stage(now + 2 * day)));
        CompetitionSnapshot recentPast = competition("recent-past", List.of(stage(now - 2 * day)));
        CompetitionSnapshot olderPast = competition("older-past", List.of(stage(now - 30 * day)));
        CompetitionSnapshot noStages = competition("no-stages", List.of());
        when(getCompetitionListPersistencePort.getCompetitions("user-1"))
                .thenReturn(List.of(olderPast, noStages, upcomingLater, recentPast, upcomingSoon));

        List<FetchCompetitionDTO> result = serviceCase.getCompetitions("user-1", true);

        assertThat(result).extracting(FetchCompetitionDTO::id)
                .containsExactly("upcoming-soon", "upcoming-later", "recent-past", "older-past", "no-stages");
    }

    private StageSnapshot stage(long dateFrom) {
        return new StageSnapshot("stage-" + dateFrom, "Stage", "comp-1", "user-1",
                dateFrom, dateFrom, 0L, 0L, null, List.of());
    }

    @Test
    void maps_competition_with_finished_stage_as_finished() {
        // dateTo = 0L (1970) is strictly before today's UTC day -> FINISHED stage -> FINISHED competition.
        StageSnapshot finishedStage = new StageSnapshot("stage-1", "Stage 1", "comp-1", "user-1",
                0L, 0L, 0L, 0L, null, List.of());
        when(getCompetitionListPersistencePort.getCompetitions("user-1"))
                .thenReturn(List.of(competition("comp-1", List.of(finishedStage))));

        List<FetchCompetitionDTO> result = serviceCase.getCompetitions("user-1", true);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo("FINISHED");
        assertThat(result.getFirst().stages()).hasSize(1);
    }
}
