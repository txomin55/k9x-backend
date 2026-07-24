package com.k9x.infrastructure.in.rest.endpoints.secured.notifications;

import com.k9x.application.notifications.use_case.MarkNotificationsSeenServiceCase;
import com.k9x.application.notifications.use_case.command.MarkNotificationsSeenCommand;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredNotificationsMarkSeenApiDelegate;
import com.k9x.oas.stub.model.MarkNotificationsSeenRequestDTO;
import org.springframework.http.ResponseEntity;

public class MarkNotificationsSeen implements SecuredNotificationsMarkSeenApiDelegate {

    private final MarkNotificationsSeenServiceCase markNotificationsSeenServiceCase;
    private final UserInfoDTO userDetails;

    public MarkNotificationsSeen(MarkNotificationsSeenServiceCase markNotificationsSeenServiceCase, UserInfoDTO userDetails) {
        this.markNotificationsSeenServiceCase = markNotificationsSeenServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<Void> markNotificationsSeenSecured(MarkNotificationsSeenRequestDTO body) {
        markNotificationsSeenServiceCase.markSeen(
                new MarkNotificationsSeenCommand(body.getMarkSeen()),
                userDetails.getEmail());
        return ResponseEntity.ok().build();
    }
}
