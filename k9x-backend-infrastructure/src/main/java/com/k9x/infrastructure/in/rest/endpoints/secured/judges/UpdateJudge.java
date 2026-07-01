package com.k9x.infrastructure.in.rest.endpoints.secured.judges;

import com.k9x.application.judges.use_case.command.UpdateJudgeCommand;
import com.k9x.application.judges.use_case.UpdateJudgeServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
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
        // TODO: `country` is declared for judge update in oas.yaml (UpdateJudgeRequestDTO) but the published
        //  oas-definition-stubs jar does not expose getCountry() yet. Once the stub is republished, replace
        //  the fixed value below with body.getCountry().
        String country = "";
        updateJudgeServiceCase.updateJudge(id, new UpdateJudgeCommand(body.getName(), country), userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
