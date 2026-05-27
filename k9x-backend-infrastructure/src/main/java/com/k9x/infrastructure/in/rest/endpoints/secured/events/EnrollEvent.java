package com.k9x.infrastructure.in.rest.endpoints.secured.events;

import com.k9x.application.events.obdx.use_case.EnrollObdxEventServiceCase;
import com.k9x.application.events.obdx.use_case.command.EnrollObdxEventCommand;
import com.k9x.oas.stub.api.SecuredEventsEnrollApiDelegate;
import com.k9x.oas.stub.model.EnrollStageEventRequestDTO;
import org.springframework.http.ResponseEntity;

public class EnrollEvent implements SecuredEventsEnrollApiDelegate {

    private final EnrollObdxEventServiceCase enrollObdxEventServiceCase;

    public EnrollEvent(EnrollObdxEventServiceCase enrollObdxEventServiceCase) {
        this.enrollObdxEventServiceCase = enrollObdxEventServiceCase;
    }

    @Override
    public ResponseEntity<Object> doEnrollSecured(String stageId, String eventId, EnrollStageEventRequestDTO body) {
        enrollObdxEventServiceCase.enrollEvent(eventId, new EnrollObdxEventCommand(body.getDogId()));
        return ResponseEntity.ok().build();
    }
}
