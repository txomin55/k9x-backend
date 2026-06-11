package com.k9x.domain.competitions.status;

import com.k9x.domain.competitions.aggregates.CompetitionSnapshot;
import com.k9x.domain.stages.aggregates.StageSnapshot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompetitionStatusTest {

    private static final long NOW = Instant.parse("2024-06-15T12:00:00Z").toEpochMilli();
    private static final long TODAY = Instant.parse("2024-06-15T08:00:00Z").toEpochMilli();
    private static final long YESTERDAY = Instant.parse("2024-06-14T08:00:00Z").toEpochMilli();
    private static final long NEXT_WEEK = Instant.parse("2024-06-22T08:00:00Z").toEpochMilli();

    private static CompetitionSnapshot competition(Long deletedAt, List<StageSnapshot> stages) {
        return new CompetitionSnapshot("c1", "WC", "creator", "Org", "ES", "desc", "addr", null, null, 0L, 0L, deletedAt, stages);
    }

    private static StageSnapshot stage(long dateFrom, long dateTo, Long deletedAt) {
        return new StageSnapshot("s", "Stage", "c1", "creator", dateFrom, dateTo, 0L, 0L, deletedAt, List.of());
    }

    @Test
    void deleted_when_deleted_at_is_set() {
        assertEquals(CompetitionStatus.DELETED, competition(1L, List.of()).status(NOW));
    }

    @Test
    void created_when_there_are_no_active_stages() {
        assertEquals(CompetitionStatus.CREATED, competition(null, List.of()).status(NOW));
    }

    @Test
    void finished_when_all_active_stages_are_finished() {
        assertEquals(CompetitionStatus.COMPLETED,
                competition(null, List.of(stage(YESTERDAY, YESTERDAY, null), stage(YESTERDAY, YESTERDAY, null))).status(NOW));
    }

    @Test
    void started_when_any_stage_is_to_start_or_started() {
        assertEquals(CompetitionStatus.STARTED,
                competition(null, List.of(stage(YESTERDAY, YESTERDAY, null), stage(TODAY, NEXT_WEEK, null))).status(NOW));
    }

    @Test
    void created_when_stages_are_only_future_created_ones() {
        assertEquals(CompetitionStatus.CREATED,
                competition(null, List.of(stage(NEXT_WEEK, NEXT_WEEK, null))).status(NOW));
    }

    @Test
    void deleted_stages_are_ignored_when_deriving_status() {
        // the only stage that would be FINISHED is soft-deleted -> treated as no active stages -> CREATED
        assertEquals(CompetitionStatus.CREATED,
                competition(null, List.of(stage(YESTERDAY, YESTERDAY, 5L))).status(NOW));
    }
}
