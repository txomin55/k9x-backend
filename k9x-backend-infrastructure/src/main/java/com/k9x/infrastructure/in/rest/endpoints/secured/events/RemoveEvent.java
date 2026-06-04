package com.k9x.infrastructure.in.rest.endpoints.secured.events;

import com.k9x.application.events.use_case.DeleteEventServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsDeleteApiDelegate;
import org.springframework.http.ResponseEntity;

public class RemoveEvent implements SecuredEventsDeleteApiDelegate {

    private final DeleteEventServiceCase deleteEventServiceCase;
    private final UserInfoDTO userDetails;

    public RemoveEvent(DeleteEventServiceCase deleteEventServiceCase, UserInfoDTO userDetails) {
        this.deleteEventServiceCase = deleteEventServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> removeEventSecured(String id) {
        deleteEventServiceCase.deleteEvent(id, userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
