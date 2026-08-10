package com.k9x.application.rankings.use_case;

import com.k9x.application.rankings.port.GetRankingIncludeByListPort;
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
class GetRankingIncludeByListServiceCaseTest {

    @Mock
    private GetRankingIncludeByListPort getRankingIncludeByListPort;

    private GetRankingIncludeByListServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetRankingIncludeByListServiceCase(getRankingIncludeByListPort);
    }

    @Test
    void returns_the_criteria_from_the_port() {
        List<RankingCriterionDTO> criteria = List.of(new RankingCriterionDTO("NONE", "Every result"));
        when(getRankingIncludeByListPort.getIncludeBys()).thenReturn(criteria);

        assertThat(serviceCase.getIncludeBys()).isEqualTo(criteria);
    }
}
