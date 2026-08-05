package com.k9x.application.notifications.port.payload;

import com.k9x.application.notifications.use_case.command.CreateStageNotificationCommand;
import com.k9x.application.utils.date.DateUtils;

import java.util.List;

public record SaveEventNotificationPersistencePayload(List<String> eventIds, String content, long timestamp) {

    public static SaveEventNotificationPersistencePayload from(CreateStageNotificationCommand command) {
        return new SaveEventNotificationPersistencePayload(
                command.eventIds(), command.content(), DateUtils.nowUtcMillis());
    }
}
