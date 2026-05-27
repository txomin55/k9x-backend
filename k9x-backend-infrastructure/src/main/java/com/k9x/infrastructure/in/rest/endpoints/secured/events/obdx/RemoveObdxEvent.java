package com.k9x.infrastructure.in.rest.endpoints.secured.events.obdx;

import com.k9x.application.events.obdx.use_case.DeleteObdxEventServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredEventsDeleteApiDelegate;
import org.springframework.http.ResponseEntity;

public class RemoveObdxEvent implements SecuredEventsDeleteApiDelegate {

    private final DeleteObdxEventServiceCase deleteObdxEventServiceCase;
    private final UserInfoDTO userDetails;

    public RemoveObdxEvent(DeleteObdxEventServiceCase deleteObdxEventServiceCase, UserInfoDTO userDetails) {
        this.deleteObdxEventServiceCase = deleteObdxEventServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> removeEventSecured(String id) {
        deleteObdxEventServiceCase.deleteEvent(id, userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
