package com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx;

import com.k9x.application.events.obdx.use_case.UpdateNotCompetingServiceCase;
import com.k9x.application.events.obdx.use_case.command.UpdateNotCompetingCommand;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsUpdateNotCompetingObdxApiDelegate;
import com.k9x.oas.stub.model.UpdateEventNotCompetingRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateObdxEventNotCompeting implements SecuredEventsUpdateNotCompetingObdxApiDelegate {

    private final UpdateNotCompetingServiceCase updateNotCompetingServiceCase;
    private final UserInfoDTO userDetails;

    public UpdateObdxEventNotCompeting(UpdateNotCompetingServiceCase updateNotCompetingServiceCase, UserInfoDTO userDetails) {
        this.updateNotCompetingServiceCase = updateNotCompetingServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> updateObdxEventNotCompeting(String eventId, UpdateEventNotCompetingRequestDTO body) {
        updateNotCompetingServiceCase.updateNotCompeting(
                eventId,
                new UpdateNotCompetingCommand(body.getDogId(), Boolean.TRUE.equals(body.getNotCompeting())),
                userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
