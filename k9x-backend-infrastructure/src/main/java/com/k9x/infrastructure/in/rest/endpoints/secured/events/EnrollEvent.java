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
        enrollEventServiceCase.enrollEvent(eventId,
                new EnrollObdxEventCommand(enrollStageEventRequestDTO.getDogId(),
                        Boolean.TRUE.equals(enrollStageEventRequestDTO.getBih())),
                userDetails.getEmail());
        return ResponseEntity.ok().build();
    }
}
