package com.k9x.application.events.obdx.use_cases;

import com.k9x.application.events.obdx.port.GetClassificationPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.use_cases.dto.FetchClassificationRawRowDTO;
import com.k9x.application.events.obdx.use_cases.dto.FetchObdxClassificationDTO;
import com.k9x.application.events.obdx.use_cases.dto.ObdxClassificationConfigDTO;
import com.k9x.application.events.obdx.use_cases.port.ClassificationCacheManagerPort;
import com.k9x.domain.aggregates.disciplines.ClassificationCacheEvictStrategy;
import com.k9x.domain.aggregates.disciplines.Discipline;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetObdxClassificationServiceCaseTest {

    private static final Event ACTIVE_EVENT = new Event(
            "evt-1", "OBDX_RSCE_GRADE_1_V0", "obdx", "Open Grade 1", "stage-1", "creator@test.com",
            1000L, 1000L, null, ObdxAvgMethod.MID_AVG);
    private static final ObdxClassificationConfigDTO CONFIG = new ObdxClassificationConfigDTO(
            ClassificationCacheEvictStrategy.OBDX,
            new BigDecimal("10"),
            Map.of("ex-1", new BigDecimal("3"), "ex-2", new BigDecimal("4")),
            List.of("ex-1"),
            List.of("ex-2"));

    @Mock
    private GetObdxClassificationConfigPort getObdxClassificationConfigPort;
    @Mock
    private ClassificationCacheManagerPort classificationCacheManagerPort;
    @Mock
    private GetClassificationPersistencePort getClassificationPersistencePort;

    private GetObdxClassificationServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetObdxClassificationServiceCase(
                getObdxClassificationConfigPort,
                classificationCacheManagerPort, getClassificationPersistencePort);
    }

    @Test
    void exposes_obdx_as_its_discipline() {
        assertThat(serviceCase.discipline()).isEqualTo(Discipline.OBDX);
    }

    @Test
    void returns_cached_result_when_ttl_not_expired() {
        FetchObdxClassificationDTO cached = new FetchObdxClassificationDTO(null, List.of());

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(cached);

        FetchObdxClassificationDTO result = serviceCase.getClassification(ACTIVE_EVENT);

        assertThat(result).isSameAs(cached);
        verifyNoInteractions(getClassificationPersistencePort);
    }

    @Test
    void recomputes_and_caches_when_cache_miss() {
        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getClassificationPersistencePort.getClassification("evt-1")).thenReturn(List.of());

        serviceCase.getClassification(ACTIVE_EVENT);

        verify(getClassificationPersistencePort).getClassification("evt-1");
        verify(classificationCacheManagerPort).put(eq("evt-1"), any());
    }

    @Test
    void applies_avg_multiplied_by_coef_when_fewer_than_4_judges() {
        List<FetchClassificationRawRowDTO> rows = List.of(
                row("dog-1", "Rex", "j-1", new BigDecimal("8")),
                row("dog-1", "Rex", "j-2", new BigDecimal("6")));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getClassificationPersistencePort.getClassification("evt-1")).thenReturn(rows);

        FetchObdxClassificationDTO result = serviceCase.getClassification(ACTIVE_EVENT);

        // avg(8,6) = 7, * coef(3) = 21
        assertThat(result.competitors()).hasSize(1);
        assertThat(result.competitors().getFirst().totalScore()).isEqualByComparingTo("21.00");
    }

    @Test
    void applies_mid_avg_removing_min_and_max_when_4_or_more_judges() {
        ObdxClassificationConfigDTO midAvgConfig = new ObdxClassificationConfigDTO(
                ClassificationCacheEvictStrategy.OBDX,
                new BigDecimal("10"),
                Map.of("ex-1", new BigDecimal("2")), List.of(), List.of());

        List<FetchClassificationRawRowDTO> rows = List.of(
                row("dog-1", "Rex", "j-1", new BigDecimal("5")),
                row("dog-1", "Rex", "j-2", new BigDecimal("7")),
                row("dog-1", "Rex", "j-3", new BigDecimal("9")),
                row("dog-1", "Rex", "j-4", new BigDecimal("3")));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(midAvgConfig);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getClassificationPersistencePort.getClassification("evt-1")).thenReturn(rows);

        FetchObdxClassificationDTO result = serviceCase.getClassification(ACTIVE_EVENT);

        // remove min(3) and max(9), avg(5,7) = 6, * coef(2) = 12
        assertThat(result.competitors().getFirst().totalScore()).isEqualByComparingTo("12.00");
    }

    @Test
    void assigns_positions_sorted_by_total_score_descending() {
        List<FetchClassificationRawRowDTO> rows = List.of(
                row("dog-1", "Rex", "j-1", new BigDecimal("6")),
                row("dog-2", "Max", "j-1", new BigDecimal("9")));

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getClassificationPersistencePort.getClassification("evt-1")).thenReturn(rows);

        FetchObdxClassificationDTO result = serviceCase.getClassification(ACTIVE_EVENT);

        assertThat(result.competitors()).hasSize(2);
        assertThat(result.competitors().get(0).dogId()).isEqualTo("dog-2");
        assertThat(result.competitors().get(0).position()).isEqualTo(1);
        assertThat(result.competitors().get(1).dogId()).isEqualTo("dog-1");
        assertThat(result.competitors().get(1).position()).isEqualTo(2);
    }

    @Test
    void tied_dogs_get_same_position() {
        List<FetchClassificationRawRowDTO> rows = List.of(
                row("dog-1", "Rex", "j-1", new BigDecimal("7")),
                row("dog-2", "Max", "j-1", new BigDecimal("7")));

        ObdxClassificationConfigDTO tieConfig = new ObdxClassificationConfigDTO(
                ClassificationCacheEvictStrategy.OBDX,
                new BigDecimal("10"),
                Map.of("ex-1", new BigDecimal("1")), List.of(), List.of());

        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(tieConfig);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getClassificationPersistencePort.getClassification("evt-1")).thenReturn(rows);

        FetchObdxClassificationDTO result = serviceCase.getClassification(ACTIVE_EVENT);

        assertThat(result.competitors()).hasSize(2);
        assertThat(result.competitors().get(0).position()).isEqualTo(1);
        assertThat(result.competitors().get(1).position()).isEqualTo(1);
    }

    private FetchClassificationRawRowDTO row(String dogId, String dogName,
                                             String judgeId,
                                             BigDecimal score) {
        return new FetchClassificationRawRowDTO(
                dogId, dogName, "owner@test.com", "Team A", "ES",
                "ex-1", (short) 1, null,
                judgeId, "Judge " + judgeId,
                score, 1000L);
    }
}
