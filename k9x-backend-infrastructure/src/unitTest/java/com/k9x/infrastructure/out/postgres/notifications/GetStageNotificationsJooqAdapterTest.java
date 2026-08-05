package com.k9x.infrastructure.out.postgres.notifications;

import com.k9x.application.notifications.use_case.dto.StageNotificationDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;
import org.jooq.Record5;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GetStageNotificationsJooqAdapterTest {

    /** One row per (announcement, event) pair, which is what the join produces. */
    private record Row(String stageId, long notificationId, long timestamp, String content, String eventId) {
    }

    private MockDataProvider provider(AtomicReference<String> capturedSql,
                                      AtomicReference<Object[]> capturedBindings,
                                      List<Row> rows) {
        return ctx -> {
            capturedSql.set(ctx.sql());
            capturedBindings.set(ctx.bindings());
            DSLContext create = DSL.using(SQLDialect.POSTGRES);
            Result<Record5<String, Long, Long, String, String>> result = create.newResult(
                    Tables.EVENTS.STAGE_ID,
                    Tables.EVENT_NOTIFICATIONS.ID,
                    Tables.EVENT_NOTIFICATIONS.TIMESTAMP,
                    Tables.EVENT_NOTIFICATIONS.CONTENT,
                    Tables.EVENTS_EVENT_NOTIFICATIONS.EVENT_ID);
            rows.forEach(r -> result.add(create.newRecord(
                            Tables.EVENTS.STAGE_ID,
                            Tables.EVENT_NOTIFICATIONS.ID,
                            Tables.EVENT_NOTIFICATIONS.TIMESTAMP,
                            Tables.EVENT_NOTIFICATIONS.CONTENT,
                            Tables.EVENTS_EVENT_NOTIFICATIONS.EVENT_ID)
                    .values(r.stageId(), r.notificationId(), r.timestamp(), r.content(), r.eventId())));
            return new MockResult[]{new MockResult(result.size(), result)};
        };
    }

    @Test
    void folds_one_row_per_event_into_one_announcement_carrying_all_its_event_ids() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();
        DSLContext dsl = DSL.using(new MockConnection(provider(capturedSql, capturedBindings, List.of(
                new Row("stage-1", 20L, 2000L, "Ceremony delayed", "event-1"),
                new Row("stage-1", 20L, 2000L, "Ceremony delayed", "event-2"),
                new Row("stage-1", 10L, 1000L, "Bring your licence", "event-1")))), SQLDialect.POSTGRES);

        Map<String, List<StageNotificationDTO>> byStage =
                new GetStageNotificationsJooqAdapter(dsl).getByStageIds(List.of("stage-1"));

        assertThat(byStage).containsOnlyKeys("stage-1");
        // Newest first, as ordered by the query.
        assertThat(byStage.get("stage-1")).containsExactly(
                new StageNotificationDTO(2000L, List.of("event-1", "event-2"), "Ceremony delayed"),
                new StageNotificationDTO(1000L, List.of("event-1"), "Bring your licence"));
        assertThat(capturedSql.get())
                .contains("\"k9x\".\"event_notifications\"")
                .contains("\"k9x\".\"events_event_notifications\"")
                .contains("\"k9x\".\"events\".\"deleted_at\" is null")
                .contains("order by")
                .contains("desc");
        assertThat(capturedBindings.get()).contains("stage-1");
    }

    @Test
    void groups_announcements_by_their_stage() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();
        DSLContext dsl = DSL.using(new MockConnection(provider(capturedSql, capturedBindings, List.of(
                new Row("stage-1", 20L, 2000L, "For stage 1", "event-1"),
                new Row("stage-2", 10L, 1000L, "For stage 2", "event-9")))), SQLDialect.POSTGRES);

        Map<String, List<StageNotificationDTO>> byStage =
                new GetStageNotificationsJooqAdapter(dsl).getByStageIds(List.of("stage-1", "stage-2"));

        assertThat(byStage).containsOnlyKeys("stage-1", "stage-2");
        assertThat(byStage.get("stage-1")).extracting(StageNotificationDTO::content).containsExactly("For stage 1");
        assertThat(byStage.get("stage-2")).extracting(StageNotificationDTO::content).containsExactly("For stage 2");
    }

    @Test
    void omits_stages_without_announcements_instead_of_mapping_them_to_an_empty_list() {
        AtomicReference<String> capturedSql = new AtomicReference<>();
        AtomicReference<Object[]> capturedBindings = new AtomicReference<>();
        DSLContext dsl = DSL.using(new MockConnection(
                provider(capturedSql, capturedBindings, List.of())), SQLDialect.POSTGRES);

        assertThat(new GetStageNotificationsJooqAdapter(dsl).getByStageIds(List.of("stage-1"))).isEmpty();
    }

    @Test
    void hits_no_database_when_there_are_no_stages() {
        DSLContext dsl = DSL.using(new MockConnection(ctx -> {
            throw new AssertionError("no query expected for an empty stage list");
        }), SQLDialect.POSTGRES);

        assertThat(new GetStageNotificationsJooqAdapter(dsl).getByStageIds(List.of())).isEmpty();
        assertThat(new GetStageNotificationsJooqAdapter(dsl).getByStageIds(null)).isEmpty();
    }
}
