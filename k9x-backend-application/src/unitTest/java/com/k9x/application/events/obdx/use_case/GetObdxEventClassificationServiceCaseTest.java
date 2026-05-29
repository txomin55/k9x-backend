package com.k9x.application.events.obdx.use_case;

import com.k9x.application.events.exceptions.EventAlreadyDeletedException;
import com.k9x.application.events.exceptions.EventNotFoundException;
import com.k9x.application.events.obdx.port.ClassificationCacheManagerPort;
import com.k9x.application.events.obdx.port.GetClassificationPersistencePort;
import com.k9x.application.events.obdx.port.GetObdxClassificationConfigPort;
import com.k9x.application.events.obdx.port.GetObdxEventPersistencePort;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationDTO;
import com.k9x.application.events.obdx.use_case.dto.FetchClassificationRawRowDTO;
import com.k9x.application.events.obdx.use_case.dto.ObdxClassificationConfigDTO;
import com.k9x.application.stages.port.GetStagePersistencePort;
import com.k9x.domain.aggregates.disciplines.ClassificationCacheEvictStrategy;
import com.k9x.domain.aggregates.disciplines.obdx.ObdxAvgMethod;
import com.k9x.domain.aggregates.events.obdx.ObdxEvent;
import com.k9x.domain.aggregates.stages.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetObdxEventClassificationServiceCaseTest {

    private static final ObdxEvent ACTIVE_EVENT = new ObdxEvent(
            "evt-1", "OBDX_RSCE_GRADE_1_V0", "Open Grade 1", "stage-1", "creator@test.com",
            1000L, 1000L, null, ObdxAvgMethod.MID_AVG);
    private static final Stage STAGE = new Stage(
            "stage-1", "Stage A", "comp-1", "user-1", Long.MAX_VALUE, 1000L, 1000L, null);
    private static final ObdxClassificationConfigDTO CONFIG = new ObdxClassificationConfigDTO(
            ClassificationCacheEvictStrategy.OBDX,
            new BigDecimal("10"),
            Map.of("ex-1", new BigDecimal("3"), "ex-2", new BigDecimal("4")),
            List.of("ex-1"),
            List.of("ex-2"));

    @Mock
    private GetObdxEventPersistencePort getObdxEventPersistencePort;
    @Mock
    private GetStagePersistencePort getStagePersistencePort;
    @Mock
    private GetObdxClassificationConfigPort getObdxClassificationConfigPort;
    @Mock
    private ClassificationCacheManagerPort classificationCacheManagerPort;
    @Mock
    private GetClassificationPersistencePort getClassificationPersistencePort;

    private GetObdxEventClassificationServiceCase serviceCase;

    @BeforeEach
    void setUp() {
        serviceCase = new GetObdxEventClassificationServiceCase(
                getObdxEventPersistencePort, getStagePersistencePort,
                getObdxClassificationConfigPort, classificationCacheManagerPort,
                getClassificationPersistencePort);
    }

    @Test
    void throws_exception_when_event_not_found() {
        when(getObdxEventPersistencePort.getEvent("evt-1")).thenReturn(null);

        assertThatThrownBy(() -> serviceCase.getClassification("evt-1"))
                .isInstanceOf(EventNotFoundException.class);

        verifyNoInteractions(classificationCacheManagerPort, getClassificationPersistencePort);
    }

    @Test
    void throws_exception_when_event_is_deleted() {
        ObdxEvent deleted = new ObdxEvent("evt-1", "OBDX_RSCE_GRADE_1_V0", "Open", "stage-1",
                "creator@test.com", 1000L, 1000L, 9999L, ObdxAvgMethod.MID_AVG);
        when(getObdxEventPersistencePort.getEvent("evt-1")).thenReturn(deleted);

        assertThatThrownBy(() -> serviceCase.getClassification("evt-1"))
                .isInstanceOf(EventAlreadyDeletedException.class);

        verifyNoInteractions(classificationCacheManagerPort, getClassificationPersistencePort);
    }

    @Test
    void returns_cached_result_when_ttl_not_expired() {
        FetchClassificationDTO cached = new FetchClassificationDTO(
                "evt-1", "Open", "stage-1", "Stage A", "OBDX_RSCE_GRADE_1_V0",
                null, List.of());

        when(getObdxEventPersistencePort.getEvent("evt-1")).thenReturn(ACTIVE_EVENT);
        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(cached);

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

        assertThat(result).isSameAs(cached);
        verifyNoInteractions(getClassificationPersistencePort);
    }

    @Test
    void recomputes_and_caches_when_cache_miss() {
        when(getObdxEventPersistencePort.getEvent("evt-1")).thenReturn(ACTIVE_EVENT);
        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(STAGE);
        when(getClassificationPersistencePort.getClassification("evt-1")).thenReturn(List.of());

        serviceCase.getClassification("evt-1");

        verify(getClassificationPersistencePort).getClassification("evt-1");
        verify(classificationCacheManagerPort).put(eq("evt-1"), any());
    }

    @Test
    void applies_avg_multiplied_by_coef_when_fewer_than_4_judges() {
        List<FetchClassificationRawRowDTO> rows = List.of(
                row("dog-1", "Rex", "j-1", new BigDecimal("8")),
                row("dog-1", "Rex", "j-2", new BigDecimal("6")));

        when(getObdxEventPersistencePort.getEvent("evt-1")).thenReturn(ACTIVE_EVENT);
        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(STAGE);
        when(getClassificationPersistencePort.getClassification("evt-1")).thenReturn(rows);

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

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

        when(getObdxEventPersistencePort.getEvent("evt-1")).thenReturn(ACTIVE_EVENT);
        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(midAvgConfig);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(STAGE);
        when(getClassificationPersistencePort.getClassification("evt-1")).thenReturn(rows);

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

        // remove min(3) and max(9), avg(5,7) = 6, * coef(2) = 12
        assertThat(result.competitors().getFirst().totalScore()).isEqualByComparingTo("12.00");
    }

    @Test
    void assigns_positions_sorted_by_total_score_descending() {
        List<FetchClassificationRawRowDTO> rows = List.of(
                row("dog-1", "Rex", "j-1", new BigDecimal("6")),
                row("dog-2", "Max", "j-1", new BigDecimal("9")));

        when(getObdxEventPersistencePort.getEvent("evt-1")).thenReturn(ACTIVE_EVENT);
        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(CONFIG);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(STAGE);
        when(getClassificationPersistencePort.getClassification("evt-1")).thenReturn(rows);

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

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

        when(getObdxEventPersistencePort.getEvent("evt-1")).thenReturn(ACTIVE_EVENT);
        when(getObdxClassificationConfigPort.getConfig("OBDX_RSCE_GRADE_1_V0")).thenReturn(tieConfig);
        when(classificationCacheManagerPort.getIfPresentAndValid(eq("evt-1"), anyInt())).thenReturn(null);
        when(getStagePersistencePort.getStage("stage-1")).thenReturn(STAGE);
        when(getClassificationPersistencePort.getClassification("evt-1")).thenReturn(rows);

        FetchClassificationDTO result = serviceCase.getClassification("evt-1");

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
