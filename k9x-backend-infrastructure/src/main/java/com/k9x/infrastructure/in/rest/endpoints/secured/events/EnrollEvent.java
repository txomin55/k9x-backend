package com.k9x.infrastructure.in.rest.endpoints.secured.events;

import com.k9x.application.events.obdx.use_cases.command.EnrollObdxEventCommand;
import com.k9x.application.events.use_cases.EnrollEventServiceCase;
import com.k9x.oas.stub.api.SecuredEventsEnrollApiDelegate;
import com.k9x.oas.stub.model.EnrollStageEventRequestDTO;
import org.springframework.http.ResponseEntity;

public class EnrollEvent implements SecuredEventsEnrollApiDelegate {

    private final EnrollEventServiceCase enrollEventServiceCase;

    public EnrollEvent(EnrollEventServiceCase enrollEventServiceCase) {
        this.enrollEventServiceCase = enrollEventServiceCase;
    }

    @Override
    public ResponseEntity<Object> doEnrollSecured(String eventId, EnrollStageEventRequestDTO enrollStageEventRequestDTO) {
        enrollEventServiceCase.enrollEvent(eventId, new EnrollObdxEventCommand(enrollStageEventRequestDTO.getDogId()));
        return ResponseEntity.ok().build();
    }
}
