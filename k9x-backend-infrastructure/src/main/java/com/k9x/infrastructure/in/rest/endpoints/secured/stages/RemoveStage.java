package com.k9x.infrastructure.in.rest.endpoints.secured.stages;

import com.k9x.application.stages.use_case.DeleteStageServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredStagesRemoveApiDelegate;
import org.springframework.http.ResponseEntity;

public class RemoveStage implements SecuredStagesRemoveApiDelegate {

    private final DeleteStageServiceCase deleteStageServiceCase;
    private final UserInfoDTO userDetails;

    public RemoveStage(DeleteStageServiceCase deleteStageServiceCase, UserInfoDTO userDetails) {
        this.deleteStageServiceCase = deleteStageServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> removeStage(String id) {
        deleteStageServiceCase.deleteStage(id, userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
