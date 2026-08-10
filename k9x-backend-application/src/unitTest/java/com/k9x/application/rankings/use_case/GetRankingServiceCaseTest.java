package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.port.GetRankingDetailPersistencePort;
import com.k9x.application.rankings.use_case.dto.FetchRankingDTO;
import com.k9x.domain.exceptions.UnauthorizedResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRankingServiceCaseTest {

    private static final String RANKING_ID = "ranking_comp-1";

    @Mock
    private GetRankingDetailPersistencePort getRankingDetailPersistencePort;

    private GetRankingServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetRankingServiceCase(getRankingDetailPersistencePort);
    }

    @Test
    void throws_exception_when_user_is_not_organizer() {
        assertThatThrownBy(() -> serviceCase.getRanking(RANKING_ID, "user-1", false))
                .isInstanceOf(UnauthorizedResourceException.class);

        verifyNoInteractions(getRankingDetailPersistencePort);
    }

    @Test
    void returns_empty_when_the_ranking_does_not_exist() {
        when(getRankingDetailPersistencePort.getRankingDetail(RANKING_ID, "user-1")).thenReturn(null);

        assertThat(serviceCase.getRanking(RANKING_ID, "user-1", true)).isEmpty();
    }

    @Test
    void always_scopes_the_lookup_to_the_authenticated_user() {
        FetchRankingDTO ranking = new FetchRankingDTO(RANKING_ID, "Copa", List.of(), "INDIVIDUAL", "ALL", null);
        when(getRankingDetailPersistencePort.getRankingDetail(RANKING_ID, "user-1")).thenReturn(ranking);

        assertThat(serviceCase.getRanking(RANKING_ID, "user-1", true)).contains(ranking);
    }
}
