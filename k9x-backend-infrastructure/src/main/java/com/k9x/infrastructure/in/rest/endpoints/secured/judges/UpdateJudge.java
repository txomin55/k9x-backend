package com.k9x.infrastructure.in.rest.endpoints.secured.judges;

import com.k9x.application.judges.use_case.command.UpdateJudgeCommand;
import com.k9x.application.judges.use_case.UpdateJudgeServiceCase;
import com.k9x.application.users.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredJudgesUpdateApiDelegate;
import com.k9x.oas.stub.model.UpdateJudgeRequestDTO;
import org.springframework.http.ResponseEntity;

public class UpdateJudge implements SecuredJudgesUpdateApiDelegate {

    private final UpdateJudgeServiceCase updateJudgeServiceCase;
    private final UserInfoDTO userDetails;

    public UpdateJudge(UpdateJudgeServiceCase updateJudgeServiceCase, UserInfoDTO userDetails) {
        this.updateJudgeServiceCase = updateJudgeServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> updateJudgeSecured(String id, UpdateJudgeRequestDTO body) {
        updateJudgeServiceCase.updateJudge(id, new UpdateJudgeCommand(body.getName()), userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
