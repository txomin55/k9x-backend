package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.port.GetRankingGroupByListPort;
import com.k9x.application.rankings.use_case.dto.RankingCriterionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRankingGroupByListServiceCaseTest {

    @Mock
    private GetRankingGroupByListPort getRankingGroupByListPort;

    private GetRankingGroupByListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetRankingGroupByListServiceCase(getRankingGroupByListPort);
    }

    @Test
    void returns_the_criteria_from_the_port() {
        List<RankingCriterionDTO> criteria = List.of(new RankingCriterionDTO("INDIVIDUAL", "Individual"));
        when(getRankingGroupByListPort.getGroupBys()).thenReturn(criteria);

        assertThat(serviceCase.getGroupBys()).isEqualTo(criteria);
    }
}
