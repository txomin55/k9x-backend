package com.k9x.infrastructure.in.rest.endpoints.secured.judges;

import com.k9x.application.judges.use_case.CreateJudgeServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredJudgesCreateApiDelegate;
import com.k9x.oas.stub.model.CreateJudgeRequestDTO;
import org.springframework.http.ResponseEntity;

public class CreateJudge implements SecuredJudgesCreateApiDelegate {

    private final CreateJudgeServiceCase createJudgeServiceCase;
    private final UserInfoDTO userDetails;

    public CreateJudge(CreateJudgeServiceCase createJudgeServiceCase, UserInfoDTO userDetails) {
        this.createJudgeServiceCase = createJudgeServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> createJudgeSecured(CreateJudgeRequestDTO body) {
        createJudgeServiceCase.createJudge(body.getId(), body.getName(), body.getCountry(), userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
