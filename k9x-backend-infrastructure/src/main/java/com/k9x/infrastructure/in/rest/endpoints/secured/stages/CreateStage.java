package com.k9x.infrastructure.in.rest.endpoints.secured.stages;

import com.k9x.application.stages.use_case.CreateStageServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredStagesCreateApiDelegate;
import com.k9x.oas.stub.model.CreateStageRequestDTO;
import org.springframework.http.ResponseEntity;

public class CreateStage implements SecuredStagesCreateApiDelegate {

    private final CreateStageServiceCase createStageServiceCase;
    private final UserInfoDTO userDetails;

    public CreateStage(CreateStageServiceCase createStageServiceCase, UserInfoDTO userDetails) {
        this.createStageServiceCase = createStageServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> createStage(CreateStageRequestDTO body) {
        createStageServiceCase.createStage(
                body.getId(),
                body.getName(),
                body.getCompetitionId(),
                body.getDateFrom(),
                body.getDateTo(),
                userDetails.getEmail(),
                userDetails.isOrganizer()
        );
        return ResponseEntity.ok().build();
    }
}
