package com.k9x.infrastructure.out.postgres.notifications;

import com.k9x.application.notifications.port.GetStageNotificationsPersistencePort;
import com.k9x.application.notifications.use_case.dto.StageNotificationDTO;
import com.k9x.infrastructure.out.postgres.jooq.generated.k9x.Tables;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a stage's announcements by walking {@code event_notifications → events_event_notifications →
 * events}, which is how an announcement is attributed to a stage: it is linked to events, and every event
 * belongs to exactly one stage.
 *
 * <p>The join yields one row per (announcement, event) pair, so rows are folded back into one entry per
 * announcement carrying all of its event ids. Soft-deleted events are excluded, which also drops an
 * announcement whose every event has been deleted.
 */
public class GetStageNotificationsJooqAdapter implements GetStageNotificationsPersistencePort {

    private final DSLContext dsl;

    public GetStageNotificationsJooqAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Map<String, List<StageNotificationDTO>> getByStageIds(List<String> stageIds) {
        if (stageIds == null || stageIds.isEmpty()) {
            return Map.of();
        }
        var rows = dsl.select(
                        Tables.EVENTS.STAGE_ID,
                        Tables.EVENT_NOTIFICATIONS.ID,
                        Tables.EVENT_NOTIFICATIONS.TIMESTAMP,
                        Tables.EVENT_NOTIFICATIONS.CONTENT,
                        Tables.EVENTS_EVENT_NOTIFICATIONS.EVENT_ID)
                .from(Tables.EVENT_NOTIFICATIONS)
                .join(Tables.EVENTS_EVENT_NOTIFICATIONS)
                .on(Tables.EVENTS_EVENT_NOTIFICATIONS.EVENT_NOTIFICATION_ID.eq(Tables.EVENT_NOTIFICATIONS.ID))
                .join(Tables.EVENTS).on(Tables.EVENTS.ID.eq(Tables.EVENTS_EVENT_NOTIFICATIONS.EVENT_ID))
                .where(Tables.EVENTS.STAGE_ID.in(stageIds))
                .and(Tables.EVENTS.DELETED_AT.isNull())
                .orderBy(Tables.EVENT_NOTIFICATIONS.TIMESTAMP.desc(), Tables.EVENT_NOTIFICATIONS.ID.desc())
                .fetch();

        // Insertion order follows the timestamp-descending query, so each stage's list stays newest first.
        Map<Long, Announcement> byId = new LinkedHashMap<>();
        rows.forEach(row -> byId.computeIfAbsent(
                        row.value2(),
                        _ -> new Announcement(row.value1(), row.value3(), row.value4(), new ArrayList<>()))
                .eventIds()
                .add(row.value5()));

        Map<String, List<StageNotificationDTO>> byStage = new LinkedHashMap<>();
        byId.values().forEach(announcement -> byStage
                .computeIfAbsent(announcement.stageId(), _ -> new ArrayList<>())
                .add(new StageNotificationDTO(
                        announcement.timestamp(), List.copyOf(announcement.eventIds()), announcement.content())));
        return byStage;
    }

    private record Announcement(String stageId, long timestamp, String content, List<String> eventIds) {
    }
}
