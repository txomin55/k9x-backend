package com.k9x.infrastructure.in.rest.endpoints.secured.judges;

import com.k9x.application.judges.use_case.CreateJudgeServiceCase;
import com.k9x.application.users.use_case.dto.UserInfoDTO;
import com.k9x.oas.stub.api.SecuredJudgesCreateApiDelegate;
import com.k9x.oas.stub.model.IdNameDTO;
import org.springframework.http.ResponseEntity;

public class CreateJudge implements SecuredJudgesCreateApiDelegate {

    private final CreateJudgeServiceCase createJudgeServiceCase;
    private final UserInfoDTO userDetails;

    public CreateJudge(CreateJudgeServiceCase createJudgeServiceCase, UserInfoDTO userDetails) {
        this.createJudgeServiceCase = createJudgeServiceCase;
        this.userDetails = userDetails;
    }

    @Override
    public ResponseEntity<String> createJudgeSecured(IdNameDTO body) {
        // TODO: `country` is declared for judge create in oas.yaml (CreateJudgeRequestDTO) but the published
        //  oas-definition-stubs jar still maps this endpoint to IdNameDTO, which has no country. Once the stub
        //  is republished, switch the body type to CreateJudgeRequestDTO and use body.getCountry().
        String country = "";
        createJudgeServiceCase.createJudge(body.getId(), body.getName(), country, userDetails.getEmail(), userDetails.isOrganizer());
        return ResponseEntity.ok().build();
    }
}
