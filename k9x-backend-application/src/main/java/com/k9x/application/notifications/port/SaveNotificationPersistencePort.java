package com.k9x.application.notifications.port;

import com.k9x.application.notifications.port.payload.SaveNotificationPersistencePayload;

public interface SaveNotificationPersistencePort {
    void save(SaveNotificationPersistencePayload payload);
}
