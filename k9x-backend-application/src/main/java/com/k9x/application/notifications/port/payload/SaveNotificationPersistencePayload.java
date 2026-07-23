package com.k9x.application.notifications.port.payload;

import com.k9x.application.notifications.valueobjects.NotificationType;
import com.k9x.application.notifications.valueobjects.PushNotification;
import com.k9x.application.utils.date.DateUtils;

import java.util.Map;

public record SaveNotificationPersistencePayload(String userId, NotificationType type,
        Map<String, String> metadata, long createdAt) {

    public static SaveNotificationPersistencePayload from(String recipientUserId, PushNotification notification) {
        return new SaveNotificationPersistencePayload(
                recipientUserId, notification.type(), notification.metadata(), DateUtils.nowUtcMillis());
    }
}
