package com.k9x.infrastructure.in.rest.endpoints.secured.stages;

import com.k9x.application.notifications.use_case.CreateStageNotificationsServiceCase;
import com.k9x.application.notifications.use_case.command.CreateStageNotificationCommand;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredStagesNotificationsCreateApiDelegate;
import com.k9x.oas.stub.model.CreateNotificationRequestDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class CreateStageNotifications implements SecuredStagesNotificationsCreateApiDelegate {

    private final CreateStageNotificationsServiceCase createStageNotificationsServiceCase;
    private final UserInfoDTO userDetails;

    public CreateStageNotifications(CreateStageNotificationsServiceCase createStageNotificationsServiceCase,
                                    UserInfoDTO userDetails) {
        this.createStageNotificationsServiceCase = createStageNotificationsServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> createStageNotifications(String id, List<CreateNotificationRequestDTO> body) {
        createStageNotificationsServiceCase.createStageNotifications(id,
                body.stream()
                        .map(dto -> new CreateStageNotificationCommand(dto.getEventIds(), dto.getContent()))
                        .toList(),
                userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
