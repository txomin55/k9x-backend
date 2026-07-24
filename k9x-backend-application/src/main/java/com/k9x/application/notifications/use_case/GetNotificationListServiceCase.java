package com.k9x.application.notifications.use_case;

import com.k9x.application.notifications.port.GetNotificationListPersistencePort;
import com.k9x.application.notifications.use_case.dto.NotificationDTO;

import java.util.List;

/**
 * Lists the authenticated user's notifications, newest first. Read-only: a user only ever sees their
 * own notifications, so scoping happens in the persistence adapter by {@code user_id}.
 */
public class GetNotificationListServiceCase {

    private final GetNotificationListPersistencePort getNotificationListPersistencePort;

    public GetNotificationListServiceCase(GetNotificationListPersistencePort getNotificationListPersistencePort) {
        this.getNotificationListPersistencePort = getNotificationListPersistencePort;
    }

    public List<NotificationDTO> getNotifications(String userId) {
        return getNotificationListPersistencePort.getByUserId(userId);
    }
}
