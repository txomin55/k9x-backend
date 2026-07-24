package com.k9x.application.notifications.port;

import java.util.List;

public interface MarkNotificationsSeenPersistencePort {

    void markSeen(String userId, List<String> notificationIds);
}
