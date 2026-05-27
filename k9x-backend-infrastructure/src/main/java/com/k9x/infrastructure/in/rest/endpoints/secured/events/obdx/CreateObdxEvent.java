package com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx;

import com.k9x.application.events.obdx.use_case.CreateObdxEventServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsCreateApiDelegate;
import com.k9x.oas.stub.model.CreateEventRequestDTO;
import org.springframework.http.ResponseEntity;

public class CreateObdxEvent implements SecuredEventsCreateApiDelegate {

    private final CreateObdxEventServiceCase createObdxEventServiceCase;
    private final UserInfoDTO userDetails;

    public CreateObdxEvent(CreateObdxEventServiceCase createObdxEventServiceCase, UserInfoDTO userDetails) {
        this.createObdxEventServiceCase = createObdxEventServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> createEventSecured(CreateEventRequestDTO body) {
        createObdxEventServiceCase.createEvent(
                body.getId(),
                body.getName(),
                body.getStageId(),
                userDetails.getEmail(),
                userDetails.isOrganizer()
        );
        return ResponseEntity.ok().build();
    }
}
