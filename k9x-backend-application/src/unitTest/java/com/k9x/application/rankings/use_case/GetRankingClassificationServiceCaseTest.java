package com.k9x.application.rankings.use_case;

import com.k9x.domain.competitions.aggregates.CompetitionSource;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationCompetitorDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.use_case.GetEventClassificationServiceCase;
import com.k9x.application.rankings.port.GetRankingPersistencePort;
import com.k9x.application.rankings.use_case.dto.FetchRankingClassificationDTO;
import com.k9x.domain.rankings.RankingGroupBy;
import com.k9x.domain.rankings.RankingIncludeBy;
import com.k9x.domain.rankings.aggregates.Ranking;
import com.k9x.domain.rankings.exceptions.RankingNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRankingClassificationServiceCaseTest {

    private static final String RANKING_ID = "ranking_comp-1";

    @Mock
    private GetRankingPersistencePort getRankingPersistencePort;

    @Mock
    private GetEventClassificationServiceCase getEventClassificationServiceCase;

    private GetRankingClassificationServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetRankingClassificationServiceCase(getRankingPersistencePort,
                getEventClassificationServiceCase);
    }

    private Ranking ranking(List<String> eventIds, RankingGroupBy groupBy) {
        return new Ranking(RANKING_ID, "Copa", eventIds, groupBy, RankingIncludeBy.ALL, null, true, "user-1",
                0L);
    }

    private FetchClassificationDTO classification(String eventId, String dog, String score) {
        FetchClassificationCompetitorDTO competitor = new FetchClassificationCompetitorDTO(
                dog, dog.toUpperCase(), "border-collie", "owner@x.com", "handler", "alpha", "ES",
                (short) 1, (short) 1, 1, new BigDecimal(score), BigDecimal.TEN, false, "SETTLED",
                false, false, false, List.of(), List.of(), "EXC", BigDecimal.ONE);
        return new FetchClassificationDTO(eventId, "Event " + eventId, "FINISHED", "stage-1", "Stage 1",
                "Copa", "OBDX", "OBDX_FCI_GRADE_1", "Grade 1", 0L,
                new FetchObdxClassificationDTO(0L, List.of(competitor), "AVG", List.of()), "A",
                CompetitionSource.API);
    }

    @Test
    void returns_empty_when_the_ranking_does_not_exist() {
        when(getRankingPersistencePort.getRanking(RANKING_ID)).thenReturn(null);

        assertThat(serviceCase.getRankingClassification(RANKING_ID)).isEmpty();

        verifyNoInteractions(getEventClassificationServiceCase);
    }

    @Test
    void does_not_scope_the_lookup_by_creator() {
        when(getRankingPersistencePort.getRanking(RANKING_ID)).thenReturn(null);

        serviceCase.getRankingClassification(RANKING_ID);

        // A public read takes the ranking by id only: there is no user on the request to scope it with.
        verify(getRankingPersistencePort).getRanking(RANKING_ID);
    }

    @Test
    void reuses_the_event_classification_use_case_once_per_event() {
        when(getRankingPersistencePort.getRanking(RANKING_ID))
                .thenReturn(ranking(List.of("event-1", "event-2"), RankingGroupBy.INDIVIDUAL));
        when(getEventClassificationServiceCase.getClassification("event-1"))
                .thenReturn(classification("event-1", "rex", "100"));
        when(getEventClassificationServiceCase.getClassification("event-2"))
                .thenReturn(classification("event-2", "rex", "80"));

        FetchRankingClassificationDTO result = serviceCase.getRankingClassification(RANKING_ID).orElseThrow();

        verify(getEventClassificationServiceCase).getClassification("event-1");
        verify(getEventClassificationServiceCase).getClassification("event-2");
        assertThat(result.events()).extracting(event -> event.id()).containsExactly("event-1", "event-2");
        assertThat(result.events()).allSatisfy(event -> assertThat(event.stageId()).isEqualTo("stage-1"));
        assertThat(result.groups()).hasSize(1);
        assertThat(result.groups().getFirst().total()).isEqualByComparingTo("180");
        assertThat(result.groupBy()).isEqualTo("INDIVIDUAL");
    }

    @Test
    void drops_an_event_that_can_no_longer_be_resolved() {
        when(getRankingPersistencePort.getRanking(RANKING_ID))
                .thenReturn(ranking(List.of("event-1", "gone"), RankingGroupBy.INDIVIDUAL));
        when(getEventClassificationServiceCase.getClassification("event-1"))
                .thenReturn(classification("event-1", "rex", "100"));
        when(getEventClassificationServiceCase.getClassification("gone"))
                .thenThrow(new RankingNotFoundException());

        FetchRankingClassificationDTO result = serviceCase.getRankingClassification(RANKING_ID).orElseThrow();

        // The whole ranking must not fail because one of its events disappeared after it was configured.
        assertThat(result.events()).extracting(event -> event.id()).containsExactly("event-1");
        assertThat(result.groups().getFirst().members().getFirst().cells()).hasSize(1);
    }

    @Test
    void returns_no_groups_when_no_event_could_be_resolved() {
        when(getRankingPersistencePort.getRanking(RANKING_ID))
                .thenReturn(ranking(List.of("gone"), RankingGroupBy.INDIVIDUAL));
        when(getEventClassificationServiceCase.getClassification("gone"))
                .thenThrow(new RankingNotFoundException());

        FetchRankingClassificationDTO result = serviceCase.getRankingClassification(RANKING_ID).orElseThrow();

        assertThat(result.events()).isEmpty();
        assertThat(result.groups()).isEmpty();
    }

    @Test
    void carries_the_group_by_so_the_rest_boundary_can_translate_countries() {
        when(getRankingPersistencePort.getRanking(RANKING_ID))
                .thenReturn(ranking(List.of("event-1"), RankingGroupBy.COUNTRY));
        when(getEventClassificationServiceCase.getClassification("event-1"))
                .thenReturn(classification("event-1", "rex", "100"));

        Optional<FetchRankingClassificationDTO> result = serviceCase.getRankingClassification(RANKING_ID);

        assertThat(result.orElseThrow().groupBy()).isEqualTo("COUNTRY");
        assertThat(result.orElseThrow().groups().getFirst().id()).isEqualTo("ES");
    }
}
