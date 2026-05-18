package com.k9x.infrastructure.in.rest.endpoints.secured.stages;

import com.k9x.application.stages.use_case.command.UpdateStageCommand;
import com.k9x.application.stages.use_case.UpdateStageServiceCase;
import com.k9x.application.users.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredStagesUpdateApiDelegate;
import com.k9x.oas.stub.model.UpdateStageRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateStage implements SecuredStagesUpdateApiDelegate {

    private final UpdateStageServiceCase updateStageServiceCase;
    private final UserInfoDTO userDetails;

    public UpdateStage(UpdateStageServiceCase updateStageServiceCase, UserInfoDTO userDetails) {
        this.updateStageServiceCase = updateStageServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> updateStage(String id, UpdateStageRequestDTO body) {
        updateStageServiceCase.updateStage(id,
                new UpdateStageCommand(body.getName(), body.getDateFrom(), body.getDateTo()),
                userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
