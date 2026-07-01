package com.k9x.infrastructure.in.rest.endpoints.secured.events;

import com.k9x.application.events.obdx.use_case.command.EnrollObdxEventCommand;
import com.k9x.application.events.use_case.EnrollEventServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsEnrollApiDelegate;
import com.k9x.oas.stub.model.EnrollStageEventRequestDTO;
import org.springframework.http.ResponseEntity;

public class EnrollEvent implements SecuredEventsEnrollApiDelegate {

    private final EnrollEventServiceCase enrollEventServiceCase;
    private final UserInfoDTO userDetails;

    public EnrollEvent(EnrollEventServiceCase enrollEventServiceCase, UserInfoDTO userDetails) {
        this.enrollEventServiceCase = enrollEventServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<Object> doEnrollSecured(String eventId, EnrollStageEventRequestDTO enrollStageEventRequestDTO) {
        // TODO: `bih` is already declared in oas.yaml but the published oas-definition-stubs jar does
        //  not expose getBih() yet. Once the stub is republished, replace the fixed value below with
        //  Boolean.TRUE.equals(enrollStageEventRequestDTO.getBih()).
        boolean bih = false;
        enrollEventServiceCase.enrollEvent(eventId,
                new EnrollObdxEventCommand(enrollStageEventRequestDTO.getDogId(), bih),
                userDetails.getEmail());
        return ResponseEntity.ok().build();
    }
}
