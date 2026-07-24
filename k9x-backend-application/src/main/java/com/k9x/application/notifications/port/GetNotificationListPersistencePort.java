package com.k9x.application.notifications.port;

import com.k9x.application.notifications.use_case.dto.NotificationDTO;

import java.util.List;

public interface GetNotificationListPersistencePort {

    List<NotificationDTO> getByUserId(String userId);
}
