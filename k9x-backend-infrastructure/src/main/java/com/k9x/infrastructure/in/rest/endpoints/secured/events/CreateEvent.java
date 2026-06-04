package com.k9x.infrastructure.in.rest.endpoints.secured.events;

import com.k9x.application.events.use_case.CreateEventServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsCreateApiDelegate;
import com.k9x.oas.stub.model.CreateEventRequestDTO;
import org.springframework.http.ResponseEntity;

public class CreateEvent implements SecuredEventsCreateApiDelegate {

    private final CreateEventServiceCase createEventServiceCase;
    private final UserInfoDTO userDetails;

    public CreateEvent(CreateEventServiceCase createEventServiceCase, UserInfoDTO userDetails) {
        this.createEventServiceCase = createEventServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> createEventSecured(CreateEventRequestDTO body) {
        createEventServiceCase.createEvent(
                body.getId(),
                body.getName(),
                body.getStageId(),
                body.getDisciplineId(),
                userDetails.getEmail(),
                userDetails.isOrganizer()
        );
        return ResponseEntity.ok().build();
    }
}
