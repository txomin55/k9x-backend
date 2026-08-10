package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.port.DeleteRankingPersistencePort;
import com.k9x.application.rankings.port.GetActiveEventIdsPersistencePort;
import com.k9x.application.rankings.port.GetRankingPersistencePort;
import com.k9x.application.rankings.port.SaveRankingPersistencePort;
import com.k9x.application.rankings.port.payload.SaveRankingPersistencePayload;
import com.k9x.application.rankings.use_case.command.SaveRankingCommand;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.rankings.RankingIncludeBy;
import com.k9x.domain.rankings.RankingGroupBy;
import com.k9x.domain.rankings.aggregates.Ranking;
import com.k9x.domain.rankings.exceptions.RankingDuplicateEventException;
import com.k9x.domain.rankings.exceptions.RankingEventNotAvailableException;
import com.k9x.domain.rankings.exceptions.RankingEventsRequiredException;
import com.k9x.domain.rankings.exceptions.RankingIncludedCountRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveRankingServiceCaseTest {

    private static final String RANKING_ID = "ranking_comp-1";

    @Mock
    private GetRankingPersistencePort getRankingPersistencePort;

    @Mock
    private GetActiveEventIdsPersistencePort getActiveEventIdsPersistencePort;

    @Mock
    private SaveRankingPersistencePort saveRankingPersistencePort;

    @Mock
    private DeleteRankingPersistencePort deleteRankingPersistencePort;

    private SaveRankingServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new SaveRankingServiceCase(getRankingPersistencePort, getActiveEventIdsPersistencePort,
                saveRankingPersistencePort, deleteRankingPersistencePort);
    }

    private SaveRankingCommand command(List<String> eventIds, RankingIncludeBy includeBy, Integer includedCount) {
        return new SaveRankingCommand(RANKING_ID, "Copa", eventIds, RankingGroupBy.INDIVIDUAL, includeBy,
                includedCount, true);
    }

    private SaveRankingCommand validCommand() {
        return command(List.of("event-1", "event-2"), RankingIncludeBy.LOWEST, 1);
    }

    private Ranking existingRanking(String creator) {
        return new Ranking(RANKING_ID, "Copa", List.of("event-1"), RankingGroupBy.INDIVIDUAL,
                RankingIncludeBy.ALL, null, true, creator, 0L);
    }

    private void eventsAreActive(String... eventIds) {
        when(getActiveEventIdsPersistencePort.getActiveEventIds(anyCollection()))
                .thenReturn(Set.of(eventIds));
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.saveRanking(validCommand(), "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getRankingPersistencePort, getActiveEventIdsPersistencePort,
                saveRankingPersistencePort, deleteRankingPersistencePort);
    }

    @Test
    void throws_exception_when_event_ids_are_empty() {
        assertThatThrownBy(() -> serviceCase.saveRanking(
                command(List.of(), RankingIncludeBy.ALL, null), "user-1", true))
                .isInstanceOf(RankingEventsRequiredException.class);

        verifyNoInteractions(getActiveEventIdsPersistencePort, saveRankingPersistencePort,
                deleteRankingPersistencePort);
    }

    @Test
    void throws_exception_when_the_same_event_is_repeated() {
        assertThatThrownBy(() -> serviceCase.saveRanking(
                command(List.of("event-1", "event-1"), RankingIncludeBy.ALL, null), "user-1", true))
                .isInstanceOf(RankingDuplicateEventException.class);

        verifyNoInteractions(saveRankingPersistencePort, deleteRankingPersistencePort);
    }

    @Test
    void throws_exception_when_included_count_is_missing_and_include_by_is_not_none() {
        assertThatThrownBy(() -> serviceCase.saveRanking(
                command(List.of("event-1"), RankingIncludeBy.HIGHEST, null), "user-1", true))
                .isInstanceOf(RankingIncludedCountRequiredException.class);

        verifyNoInteractions(saveRankingPersistencePort, deleteRankingPersistencePort);
    }

    @Test
    void throws_exception_when_included_count_is_not_positive() {
        assertThatThrownBy(() -> serviceCase.saveRanking(
                command(List.of("event-1"), RankingIncludeBy.HIGHEST, 0), "user-1", true))
                .isInstanceOf(RankingIncludedCountRequiredException.class);

        verifyNoInteractions(saveRankingPersistencePort, deleteRankingPersistencePort);
    }

    @Test
    void throws_exception_when_any_event_does_not_exist_or_is_deleted() {
        eventsAreActive("event-1");

        assertThatThrownBy(() -> serviceCase.saveRanking(validCommand(), "user-1", true))
                .isInstanceOf(RankingEventNotAvailableException.class);

        verifyNoInteractions(saveRankingPersistencePort, deleteRankingPersistencePort);
    }

    @Test
    void throws_exception_when_ranking_belongs_to_another_creator() {
        eventsAreActive("event-1", "event-2");
        when(getRankingPersistencePort.getRanking(RANKING_ID)).thenReturn(existingRanking("other-user"));

        assertThatThrownBy(() -> serviceCase.saveRanking(validCommand(), "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(saveRankingPersistencePort, deleteRankingPersistencePort);
    }

    @Test
    void deletes_and_reinserts_when_ranking_already_exists() {
        eventsAreActive("event-1", "event-2");
        when(getRankingPersistencePort.getRanking(RANKING_ID)).thenReturn(existingRanking("user-1"));

        serviceCase.saveRanking(validCommand(), "user-1", true);

        InOrder order = inOrder(deleteRankingPersistencePort, saveRankingPersistencePort);
        order.verify(deleteRankingPersistencePort).deleteRanking(RANKING_ID);
        order.verify(saveRankingPersistencePort).saveRanking(any());
    }

    @Test
    void only_inserts_when_ranking_does_not_exist() {
        eventsAreActive("event-1", "event-2");
        when(getRankingPersistencePort.getRanking(RANKING_ID)).thenReturn(null);

        serviceCase.saveRanking(validCommand(), "user-1", true);

        verify(deleteRankingPersistencePort, never()).deleteRanking(RANKING_ID);
        verify(saveRankingPersistencePort).saveRanking(any());
    }

    @Test
    void persists_null_included_count_when_include_by_is_none() {
        eventsAreActive("event-1");
        when(getRankingPersistencePort.getRanking(RANKING_ID)).thenReturn(null);

        serviceCase.saveRanking(command(List.of("event-1"), RankingIncludeBy.ALL, 5), "user-1", true);

        assertThat(capturedPayload().includedCount()).isNull();
    }

    @Test
    void persists_the_command_fields_and_the_authenticated_creator() {
        eventsAreActive("event-1", "event-2");
        when(getRankingPersistencePort.getRanking(RANKING_ID)).thenReturn(null);

        serviceCase.saveRanking(validCommand(), "user-1", true);

        SaveRankingPersistencePayload payload = capturedPayload();
        assertThat(payload.id()).isEqualTo(RANKING_ID);
        assertThat(payload.name()).isEqualTo("Copa");
        assertThat(payload.eventIds()).containsExactly("event-1", "event-2");
        assertThat(payload.groupBy()).isEqualTo(RankingGroupBy.INDIVIDUAL);
        assertThat(payload.includeBy()).isEqualTo(RankingIncludeBy.LOWEST);
        assertThat(payload.includedCount()).isEqualTo(1);
        assertThat(payload.creator()).isEqualTo("user-1");
        assertThat(payload.createdAt()).isPositive();
    }

    private SaveRankingPersistencePayload capturedPayload() {
        ArgumentCaptor<SaveRankingPersistencePayload> captor =
                ArgumentCaptor.forClass(SaveRankingPersistencePayload.class);
        verify(saveRankingPersistencePort).saveRanking(captor.capture());
        return captor.getValue();
    }
}
