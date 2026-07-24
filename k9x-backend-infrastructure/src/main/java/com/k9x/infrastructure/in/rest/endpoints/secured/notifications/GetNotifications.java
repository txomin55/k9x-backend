package com.k9x.infrastructure.in.rest.endpoints.secured.notifications;

import com.k9x.application.notifications.use_case.GetNotificationListServiceCase;
import com.k9x.application.notifications.use_case.dto.NotificationDTO;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredNotificationsFetchAllApiDelegate;
import com.k9x.oas.stub.model.NotificationResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class GetNotifications implements SecuredNotificationsFetchAllApiDelegate {

    private final GetNotificationListServiceCase getNotificationListServiceCase;
    private final UserInfoDTO userDetails;

    public GetNotifications(GetNotificationListServiceCase getNotificationListServiceCase, UserInfoDTO userDetails) {
        this.getNotificationListServiceCase = getNotificationListServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<List<NotificationResponseDTO>> fetchNotificationsSecured() {
        List<NotificationResponseDTO> notifications = getNotificationListServiceCase
                .getNotifications(userDetails.getEmail())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(notifications);
    }

    private NotificationResponseDTO toResponse(NotificationDTO notification) {
        return new NotificationResponseDTO(
                notification.id(),
                notification.timestamp(),
                notification.text(),
                notification.seen());
    }
}
