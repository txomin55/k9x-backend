package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.port.DeleteRankingPersistencePort;
import com.k9x.application.rankings.port.GetRankingPersistencePort;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import com.k9x.domain.rankings.RankingIncludeBy;
import com.k9x.domain.rankings.RankingGroupBy;
import com.k9x.domain.rankings.aggregates.Ranking;
import com.k9x.domain.rankings.exceptions.RankingNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteRankingServiceCaseTest {

    private static final String RANKING_ID = "ranking_comp-1";

    @Mock
    private GetRankingPersistencePort getRankingPersistencePort;

    @Mock
    private DeleteRankingPersistencePort deleteRankingPersistencePort;

    private DeleteRankingServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new DeleteRankingServiceCase(getRankingPersistencePort, deleteRankingPersistencePort);
    }

    private Ranking ranking(String creator) {
        return new Ranking(RANKING_ID, "Copa", List.of("event-1"), RankingGroupBy.INDIVIDUAL,
                RankingIncludeBy.ALL, null, true, creator, 0L);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.deleteRanking(RANKING_ID, "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getRankingPersistencePort, deleteRankingPersistencePort);
    }

    @Test
    void throws_exception_when_ranking_not_found() {
        when(getRankingPersistencePort.getRanking(RANKING_ID)).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.deleteRanking(RANKING_ID, "user-1", true))
                .isInstanceOf(RankingNotFoundException.class);

        verifyNoInteractions(deleteRankingPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_creator() {
        when(getRankingPersistencePort.getRanking(RANKING_ID)).thenReturn(ranking("other-user"));

        assertThatThrownBy(() -> serviceCase.deleteRanking(RANKING_ID, "user-1", true))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(deleteRankingPersistencePort);
    }

    @Test
    void deletes_ranking_when_all_validations_pass() {
        when(getRankingPersistencePort.getRanking(RANKING_ID)).thenReturn(ranking("user-1"));

        serviceCase.deleteRanking(RANKING_ID, "user-1", true);

        verify(deleteRankingPersistencePort).deleteRanking(RANKING_ID);
    }
}
